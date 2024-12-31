<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="demo-form-inline" label-width="68px">
      <el-form-item label="名称">
        <el-input v-model="queryParams.name" placeholder="请输入名称"></el-input>
      </el-form-item>

      <el-form-item label="开始时间">
        <el-date-picker
          v-model="queryParams.deployTime"
          type="datetime"
          placeholder="选择日期时间">
        </el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
        <el-button type="primary" @click="onSubmit">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="buttons">
      <span>
        <el-button  type="success" plain size="small" icon="el-icon-plus">新增</el-button>
        <el-button  type="danger" plain size="small" icon="el-icon-delete">删除</el-button>
      </span>
      <el-button    size="mini" icon="el-icon-refresh">刷新</el-button>
    </div>

    <el-alert
      title="流程设计说明:"
      type="success" >
      <div>1、XML文件中的流程定义id属性用作流程定义的key参数。</div>
      <div>2、XML文件中的流程定义name属性用作流程定义的name参数。如果未给定name属性，会使用id作为name。</div>
      <div>3、当每个唯一key的流程第一次部署时，指定版本为1。对其后所有使用相同key的流程定义，部署时版本会在该key当前已部署的最高版本号基础上加1。key参数用于区分流程定义。</div>
      <div>4、id参数设置为{processDefinitionKey}:{processDefinitionVersion}:{generated-id}，其中generated-id是一个唯一数字，用以保证在集群环境下，流程定义缓存中，流程id的唯一性。</div>
    </el-alert>

    <el-table
      :data="tableData"
      border
      style="width: 100%"  @selection-change="handleSelectionChange">>
      <el-table-column
        type="selection"
        width="55">
      </el-table-column>
      <el-table-column
        fixed
        prop="date"
        label="日期"
        width="150">
      </el-table-column>
      <el-table-column
        prop="name"
        label="姓名"
        width="120">
      </el-table-column>
      <el-table-column
        prop="province"
        label="省份"
        width="120">
      </el-table-column>
      <el-table-column
        prop="city"
        label="市区"
        width="120">
      </el-table-column>
      <el-table-column
        prop="address"
        label="地址"
        width="300">
      </el-table-column>
      <el-table-column
        prop="zip"
        label="邮编"
        width="120">
      </el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        width="100">
        <template slot-scope="scope">
          <el-button @click="handleClick(scope.row)" type="text" size="small">查看</el-button>
          <el-button type="text" size="small">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <MyPagination :total="total" @pagination="getList"></MyPagination>
  </div>
</template>
<script>
import MyPagination from "@/views/flowable/test/mypagination/index.vue";
import {listDefinition} from "@/api/flowable/definition";

export default {
  components: {MyPagination},
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: '',
        deployTime: ''
      },
      loading: true,
      total: 10,
      // 流程定义表格数据
      definitionList: [],
      multipleSelection: [],
      tableData: [{
        date: '2016-05-02',
        name: '王小虎',
        province: '上海',
        city: '普陀区',
        address: '上海市普陀区金沙江路 1518 弄',
        zip: 200333
      }, {
        date: '2016-05-04',
        name: '王小虎',
        province: '上海',
        city: '普陀区',
        address: '上海市普陀区金沙江路 1517 弄',
        zip: 200333
      }, {
        date: '2016-05-01',
        name: '王小虎',
        province: '上海',
        city: '普陀区',
        address: '上海市普陀区金沙江路 1519 弄',
        zip: 200333
      }, {
        date: '2016-05-03',
        name: '王小虎',
        province: '上海',
        city: '普陀区',
        address: '上海市普陀区金沙江路 1516 弄',
        zip: 200333
      }],

    }
  },
  created() {
    this.getList();
  },
  methods: {
    onSubmit() {
      console.log('submit!');
    },
    handleClick(row) {
      console.log(row);
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    getList() {
      this.loading = true;
      listDefinition(this.queryParams).then(response => {
        this.definitionList = response.data.records;
        this.total = response.data.total;
        this.loading = false;
      });
    },
  }
}
</script>
<style>
  .buttons {
    display: flex;
    justify-content: space-between;
    margin: 5px 0px;
  }
</style>
