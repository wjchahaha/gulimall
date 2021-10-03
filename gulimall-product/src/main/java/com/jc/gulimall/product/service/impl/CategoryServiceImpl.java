package com.jc.gulimall.product.service.impl;

import com.jc.gulimall.product.service.CategoryBrandRelationService;
import com.jc.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

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
        List<CategoryEntity> level1menus = categoryEntities.stream().filter((item) ->{
             return  item.getParentCid() == 0;
        }).map((item) ->{
            item.setChildren(getChildrens(item,categoryEntities));
            return item;
        }).sorted((menu1,menu2)->{
          return  (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
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
        for(int i = 0;i<categorys.length;i++){
            count +=baseMapper.update(categorys[i],null);
        }

        return count;
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath) ;

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        //级联更新
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());

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
        //获取所有的分类
        List<CategoryEntity> all = this.list();
        //获取所有一级分类

        List<CategoryEntity> oneLevelCategory = getCatalogEntityByParentId(all,0L);

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
                    if (level2Catalogys != null){
                        catelog2VoList = level2Catalogys.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo();
                            catelog2Vo.setCatalog1Id(l2.getParentCid().toString());
                            catelog2Vo.setId(l2.getCatId().toString());
                            catelog2Vo.setName(l2.getName());
                            //查出三级分类
//                            List<CategoryEntity> level3Catelogys = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                            List<CategoryEntity> level3Catelogys = getCatalogEntityByParentId(all, l2.getCatId());
                            //封装好了
                            if (level3Catelogys != null){
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
                return res;
    }

    /**
     * all 所有分类
     * parent_id 父id
     * @param allCategory
     * @param parent_id
     * @return
     */
    public List<CategoryEntity> getCatalogEntityByParentId(List<CategoryEntity> allCategory,Long parent_id){
        List<CategoryEntity> collect = allCategory.stream().filter(item -> {
            return item.getParentCid() == parent_id;
        }).collect(Collectors.toList());
        return collect;
    }

    //225->225.P->225.P.P
    public List<Long> findParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);

        if(byId.getParentCid() !=0){
            List<Long> parentPath = findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }


    private List<CategoryEntity> getChildrens(CategoryEntity target,List<CategoryEntity> categoryEntities){

        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> {
            return item.getParentCid().equals(target.getCatId());//递归出口是 找不到那个父id和targetCatId相同得了
        }).map(menu ->{//每一个菜单x及分类，依然会有子分类
            menu.setChildren(getChildrens(menu,categoryEntities));
            return menu;
        }).sorted((menu1,menu2)->
            (menu1.getSort() == null? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());

        return collect;

    }

}