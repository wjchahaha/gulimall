<!--  -->
<template>
  <div class="">
    <el-tree
      :data="menus"
      :props="defaultProps"
      @node-click="handleNodeClick"
      :expand-on-click-node="false"
      show-checkbox
      node-key="catId"
      :default-expanded-keys="expandedKey"
      draggable
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="append(data)"
          >
            Append
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="remove(node, data)"
          >
            Delete
          </el-button>

          <el-button type="text" size="mini" @click="update(data)">
            Update
          </el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item label="计量单位">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item label="产品数量">
          <el-input
            v-model="category.productCount"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      updateNodes: [],
      maxLevle: 0,
      title: "",
      dialogType: "",
      category: {
        catId: null,
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        icon: "",
        productUnit: "",
        productCount: 0,
      },
      menus: [],
      dialogVisible: false,
      expandedKey: [],
      defaultProps: {
        children: "children",
        label: "name",
      },
    };
  },
  methods: {
    allowDrop(draggingNode, dropNode, type) {
      console.log("allow", draggingNode, dropNode, type);
      //计算要被拖拽节点的子节点的最大深度
      this.coutnNodelevel(draggingNode.data);
      //要被拖动节点的总深度
      var deep = this.maxLevle == 0 ? 1:this.maxLevle -draggingNode.data.catLevel + 1;
      
      this.maxLevle = 0;
      console.log("深度" + deep);
      if (type == "inner") {
        return deep + dropNode.level <= 3;
      } else {
        //往被拖拽节点的前后加 相当于加入父节点的里边
        return deep + dropNode.parent.level <= 3;
      }
    },
    handleDrop(draggingNode, dropNode, dropType, ev) {
      console.log("handleDrop", draggingNode, dropNode, dropType);
      //被拖拽节点的新父节点id

      let pCid = 0;
      //拖拽成功后 被拖拽节点的兄弟节点
      let brothers = null;
      if (dropType == "inner") {
        pCid = dropNode.data.catId;
        brothers = dropNode.childNodes;
      } else {
        pCid =
          dropNode.parent.data.catId == undefined
            ? 0
            : dropNode.parent.data.catId;
        brothers = dropNode.parent.childNodes;
      }
      //当前节点的最新顺序
      for (let i = 0; i < brothers.length; i++) {
        //如果遍历到当前节点
        if (brothers[i].data.catId == draggingNode.data.catId) {
          let catLevel = draggingNode.level;
          //层级变了
          if (brothers[i].level != catLevel) {
            //被拖拽层级发生变化
            catLevel = brothers[i].level;
            //修改他子节点的层级
            this.updateChrNodeLevel(brothers[i]);
          }
          this.updateNodes.push({
            catId: brothers[i].data.catId,
            sort: i,
            parentCid: pCid,
            catLevel: catLevel,
          });
        } else {
          this.updateNodes.push({ catId: brothers[i].data.catId, sort: i });
        }
      }
      console.log("updateNode", this.updateNodes);

      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updateNodes, false),
      }).then(({ data }) => {
        this.$message({
          message: "添加成功",
          type: "success",
        });
        //刷新菜单 展出新菜单
        this.updateNodes = [];
        this.getMenus();
        this.expandedKey = [pCid];
      });

      //当前拖拽节点的最新层级
    },
    //修改子节点的层级
    updateChrNodeLevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          var cNode = node.childNodes[i].data;
          this.updateChrNodeLevel(cNode);

          this.updateNodes.push({
            catId: cNode.catId,
            catLevel: node.childNodes[i].level,
          });
        }
      }
    },
    //这个函数是求节点的最大深度
    coutnNodelevel(node) {
      // console.log("Wozheng");
      //该节点不空 而且还有子节点 计算其层级
      if (node.children != null && node.children.length > 0) {
        for (let i = 0; i < node.children.length; i++) {
          //找最大层级
          if (node.children[i].catLevel > this.maxLevle) {
            this.maxLevle = node.children[i].catLevel;
          }
          this.coutnNodelevel(node.children[i]);
        }
      }
    },
    submitData() {
      if (this.dialogType == "add") {
        this.addCategory();
      } else {
        this.updateCategory();
      }
    },
    updateCategory() {
      var { catId, name, icon, productUnit, productCount } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData(
          { catId, name, icon, productUnit, productCount },
          false
        ),
      }).then(({ data }) => {
        this.$message({
          message: "修改成功",
          type: "success",
        });
        this.dialogVisible = false;
        this.getMenus();
        this.expandedKey = [this.category.parentCid];
      });
    },
    addCategory() {
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.$message({
          message: "添加成功",
          type: "success",
        });
        this.dialogVisible = false;
        this.getMenus();
        this.expandedKey = [this.category.parentCid];
      });
    },
    handleNodeClick(data) {
      // console.log(data);
    },
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      }).then(({ data }) => {
        console.log("成功拿到菜单数据", data.data);
        this.menus = data.data;
      });
    },
    append(data) {
      this.title = "添加分类";
      this.dialogType = "add";
      this.dialogVisible = true;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;
      this.category.name = "";
      this.category.icon = "";
      this.category.productUnit = "";
      this.category.productCount = 0;
      this.category.sort = 0;
      this.category.showStatus = 1;
      console.log("提交的三级数据", this.category);
    },
    remove(node, data) {
      var ids = [data.catId];
      this.$confirm(`确定要删除【${data.name}】菜单嘛？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        //确定之后发送请求
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(ids, false),
          }).then(({ data }) => {
            this.$message({
              message: "删除成功",
              type: "success",
            });
            this.getMenus();
            this.expandedKey = [node.parent.data.catId];
          });
        })
        .catch(() => {});

      console.log("remove", node, data);
    },
    update(data) {
      this.title = "修改分类";
      this.dialogVisible = true;
      this.dialogType = "update";
      //回显函数
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get",
        params: this.$http.adornParams({}),
      }).then(({ data }) => {
        console.log("回显的数据", data);
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.productCount = data.data.productCount;
        this.category.parentCid = data.data.parentCid;
      });
    },
  },
  computed: {},
  watch: {},
  created() {
    this.getMenus();
  },
  mounted() {},
  beforeCreate() {},
  beforeMount() {},
  beforeUpdate() {},
  updated() {},
  beforeDestroy() {},
  destroyed() {},
  activated() {},
};
</script>
<style >
</style>