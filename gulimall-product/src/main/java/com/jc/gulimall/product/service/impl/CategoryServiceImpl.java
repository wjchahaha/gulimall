package com.jc.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jc.gulimall.product.service.CategoryBrandRelationService;
import com.jc.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.product.dao.CategoryDao;
import com.jc.gulimall.product.entity.CategoryEntity;
import com.jc.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private StringRedisTemplate cache;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private RedissonClient redisson;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查找数据

        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //组装成父子的树形结构
        //1找到所有的一级分类
        List<CategoryEntity> level1menus = categoryEntities.stream().filter((item) -> {
            return item.getParentCid() == 0;
        }).map((item) -> {
            item.setChildren(getChildrens(item, categoryEntities));
            return item;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1menus;
    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前产品在其他地方是否被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public int updateByIds(CategoryEntity[] categorys) {
        int count = 0;
        for (int i = 0; i < categorys.length; i++) {
            count += baseMapper.update(categorys[i], null);
        }

        return count;
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        //级联更新
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

    }

    @Override
    public List<CategoryEntity> getOneLevelCategory() {
//        QueryWrapper<CategoryEntity> cat_level = new QueryWrapper<CategoryEntity>().eq("cat_level", 1);
//
//        List<CategoryEntity> list = this.list(cat_level);

        QueryWrapper<CategoryEntity> parent_cid = new QueryWrapper<CategoryEntity>().eq("parent_cid", 0);

        List<CategoryEntity> list = this.list(parent_cid);


        return list;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 要想完美使用缓存
         * 1.null值进行缓存：解决缓存穿透
         * 2.对缓存中的数据设置一个随机的过期时间：解决缓存雪崩
         * 3.对热点数据进行加锁：解决缓存击穿
         */
        //先去缓存中查
        String catalogJSON = cache.opsForValue().get("catalogJSON");
        //没有的话从缓存中查 并且放入到缓存中
        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDB;
        } else {
            System.out.println("命中缓存....直接返回...");
            //有的话查出来反序列化然后返回
            Map<String, List<Catelog2Vo>> result = JSON.
                    parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                    });

            return result;
        }
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 1.
     * 2.
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redisson.getLock("CatalogJson-lock");
        //锁住
        lock.lock();
        System.out.println("获取分布式锁成功......");

        Map<String, List<Catelog2Vo>> catalogJsonFromDb;
        try {
            catalogJsonFromDb = getCatalogJsonFromDb();
        } finally {//执行完了进行原子删锁
            lock.unlock();
        }
        return catalogJsonFromDb;

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //占好位置的同时一定要设置过期时间，如果占好位置后,执行完了业务逻辑，要删除锁的时候要断电了，就造成死锁了
        //所以为了防止断电宕机 我们要设置锁的过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = cache.opsForValue().setIfAbsent("lock", uuid, 3000, TimeUnit.SECONDS);
        if (lock) { //拿到锁
            //执行业务
            System.out.println("获取分布式锁成功......");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb;
            try {
                catalogJsonFromDb = getCatalogJsonFromDb();
            } finally {//执行完了进行原子删锁
                //lua脚本
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1])  else  return 0 end";
                //原子删锁
                cache.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"),
                        uuid);
            }
//            //获取值进行对比 成功则删除——>原子操作
//            if (uuid.equals(cache.opsForValue().get("lock"))){//会导致释放别人锁的问题
//                cache.delete("lock");
//            }

            return catalogJsonFromDb;
        } else {
            //没拿到锁则一直尝试拿 （自旋）
            System.out.println("获取分布式锁失败....正在等待重试..........");
            try {
                //防止把栈挤炸  睡一下在请求
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {

        String catalogJSON = cache.opsForValue().get("catalogJSON");

        //如果不空的话直接返回
        if (!StringUtils.isEmpty(catalogJSON)) {
            System.out.println("贡献线程中一个......");
            //有的话查出来反序列化然后返回
            Map<String, List<Catelog2Vo>> result = JSON.
                    parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                    });

            return result;
        }

        System.out.println("去数据库中查........");
        //获取所有的分类
        List<CategoryEntity> all = this.list();
        //获取所有一级分类

        List<CategoryEntity> oneLevelCategory = getCatalogEntityByParentId(all, 0L);

        Map<String, List<Catelog2Vo>> res = oneLevelCategory.stream().collect(Collectors.toMap(
                k -> {
                    return k.getCatId().toString();
                },
                v -> {
//                    QueryWrapper<CategoryEntity> list2Wrapper = new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId());
//                    List<CategoryEntity> level2Catalogys = this.list(list2Wrapper);
                    //优化：在所有的分类中parent_id是它的
                    //查出所有二级分类
                    List<CategoryEntity> level2Catalogys = getCatalogEntityByParentId(all, v.getCatId());

                    List<Catelog2Vo> catelog2VoList = null;
                    //并进行封装
                    if (level2Catalogys != null) {
                        catelog2VoList = level2Catalogys.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo();
                            catelog2Vo.setCatalog1Id(l2.getParentCid().toString());
                            catelog2Vo.setId(l2.getCatId().toString());
                            catelog2Vo.setName(l2.getName());
                            //查出三级分类
//                            List<CategoryEntity> level3Catelogys = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                            List<CategoryEntity> level3Catelogys = getCatalogEntityByParentId(all, l2.getCatId());
                            //封装好了
                            if (level3Catelogys != null) {
                                List<Catelog2Vo.Catelog3Vo> collect = level3Catelogys.stream().map(l3 -> {
                                    Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo();
                                    catelog3Vo.setCatalog2Id(l3.getParentCid().toString());
                                    catelog3Vo.setId(l3.getCatId().toString());
                                    catelog3Vo.setName(l3.getName());

                                    return catelog3Vo;
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(collect);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());

                    }

                    return catelog2VoList;
                }));

        String jsonString = JSON.toJSONString(res);
        //第一个拿到锁的线程返回之前    放入到缓存（一定要在锁住！）
        cache.opsForValue().set("catalogJSON", jsonString);
        return res;
    }

    public synchronized Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        String catalogJSON = cache.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> result = JSON.
                    parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                    });
            System.out.println("getCatalogJsonFromDB方法命中缓存....直接返回.....");
            return result;
        }


        System.out.println("去数据库中查........");
        //获取所有的分类
        List<CategoryEntity> all = this.list();
        //获取所有一级分类

        List<CategoryEntity> oneLevelCategory = getCatalogEntityByParentId(all, 0L);

        Map<String, List<Catelog2Vo>> res = oneLevelCategory.stream().collect(Collectors.toMap(
                k -> {
                    return k.getCatId().toString();
                },
                v -> {
//                    QueryWrapper<CategoryEntity> list2Wrapper = new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId());
//                    List<CategoryEntity> level2Catalogys = this.list(list2Wrapper);
                    //优化：在所有的分类中parent_id是它的
                    //查出所有二级分类
                    List<CategoryEntity> level2Catalogys = getCatalogEntityByParentId(all, v.getCatId());

                    List<Catelog2Vo> catelog2VoList = null;
                    //并进行封装
                    if (level2Catalogys != null) {
                        catelog2VoList = level2Catalogys.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo();
                            catelog2Vo.setCatalog1Id(l2.getParentCid().toString());
                            catelog2Vo.setId(l2.getCatId().toString());
                            catelog2Vo.setName(l2.getName());
                            //查出三级分类
//                            List<CategoryEntity> level3Catelogys = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                            List<CategoryEntity> level3Catelogys = getCatalogEntityByParentId(all, l2.getCatId());
                            //封装好了
                            if (level3Catelogys != null) {
                                List<Catelog2Vo.Catelog3Vo> collect = level3Catelogys.stream().map(l3 -> {
                                    Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo();
                                    catelog3Vo.setCatalog2Id(l3.getParentCid().toString());
                                    catelog3Vo.setId(l3.getCatId().toString());
                                    catelog3Vo.setName(l3.getName());

                                    return catelog3Vo;
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(collect);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());

                    }

                    return catelog2VoList;
                }));

        String jsonString = JSON.toJSONString(res);
        //第一个拿到锁的线程返回之前    放入到缓存（一定要在锁住！）
        cache.opsForValue().set("catalogJSON", jsonString);
        return res;
    }


    /**
     * all 所有分类
     * parent_id 父id
     *
     * @param allCategory
     * @param parent_id
     * @return
     */
    public List<CategoryEntity> getCatalogEntityByParentId(List<CategoryEntity> allCategory, Long parent_id) {
        List<CategoryEntity> collect = allCategory.stream().filter(item -> {
            return item.getParentCid() == parent_id;
        }).collect(Collectors.toList());
        return collect;
    }

    //225->225.P->225.P.P
    public List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);

        if (byId.getParentCid() != 0) {
            List<Long> parentPath = findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }


    private List<CategoryEntity> getChildrens(CategoryEntity target, List<CategoryEntity> categoryEntities) {

        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> {
            return item.getParentCid().equals(target.getCatId());//递归出口是 找不到那个父id和targetCatId相同得了
        }).map(menu -> {//每一个菜单x及分类，依然会有子分类
            menu.setChildren(getChildrens(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());

        return collect;

    }

}