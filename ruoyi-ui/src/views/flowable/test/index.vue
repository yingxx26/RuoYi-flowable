<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" ref="queryForm" class="demo-form-inline" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入名称"></el-input>
      </el-form-item>

      <el-form-item label="开始时间" prop="deployTime">
        <el-date-picker
          v-model="queryParams.deployTime"
          type="datetime"
          placeholder="选择日期时间">
        </el-date-picker>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button type="primary" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="buttons">
      <span>
        <el-button  type="success" plain size="small" icon="el-icon-plus" @click="handleLoadXml">新增</el-button>
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
      :data="definitionList"
      border
      style="width: 100%"  @selection-change="handleSelectionChange">>
      <el-table-column  type="selection"  width="55">  </el-table-column>

      <el-table-column prop="deploymentId" label="流程编号" width="150"></el-table-column>
      <el-table-column prop="flowKey" label="流程标识" width="150"></el-table-column>
      <el-table-column prop="category" label="流程分类" width="150"> </el-table-column>
      <el-table-column  prop="address" label="流程名称"  width="150">
        <template slot-scope="scope">
          <el-button type="text" @click="handleReadImage(scope.row.deploymentId)">
            <span>{{ scope.row.name }}</span>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="业务表单" align="center" :show-overflow-tooltip="true" width="150">
        <template slot-scope="scope">
          <el-button v-if="scope.row.formId" type="text" @click="handleForm(scope.row.formId)">
            <span>{{ scope.row.formName }}</span>
          </el-button>
          <label v-else>暂无表单</label>
        </template>
      </el-table-column>
      <el-table-column label="流程版本" align="center" width="150">
        <template slot-scope="scope">
          <el-tag size="medium" >v{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="150">
        <template slot-scope="scope">
          <el-tag type="success" v-if="scope.row.suspensionState === 1">激活</el-tag>
          <el-tag type="warning" v-if="scope.row.suspensionState === 2">挂起</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部署时间" align="center" prop="deploymentTime" width="180"/>
      <el-table-column
        fixed="right"
        label="操作"
        width="300">
        <template slot-scope="scope">
          <el-button @click="handleLoadXml(scope.row)" icon="el-icon-edit-outline" type="text" size="small">设计</el-button>
          <el-button @click="handleAddForm(scope.row)" icon="el-icon-edit-el-icon-s-promotion" type="text" size="small" v-if="scope.row.formId == null">配置主表单</el-button>
          <el-button @click="handleUpdateSuspensionState(scope.row)" icon="el-icon-video-pause" type="text" size="small" v-if="scope.row.suspensionState===1">挂起</el-button>
          <el-button @click="handleUpdateSuspensionState(scope.row)" icon="el-icon-video-play" type="text" size="small" v-if="scope.row.suspensionState === 2">激活</el-button>
          <el-button @click="handleDelete(scope.row)" icon="el-icon-delete" type="text" size="small" v-hasPermi="['system:deployment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <MyPagination :total="total" @pagination="getList"></MyPagination>

    <!--挂载表单-->
    <el-dialog :title="formDeployTitle" :visible.sync="formDeployOpen" width="60%" append-to-body>
      <el-row :gutter="24">
        <el-col :span="10" :xs="24">
          <el-table
            ref="singleTable"
            :data="formList"
            border
            highlight-current-row
            @current-change="handleCurrentChange"
            style="width: 100%">
            <el-table-column label="表单编号" align="center" prop="formId" />
            <el-table-column label="表单名称" align="center" prop="formName" />
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="submitFormDeploy(scope.row)">确定</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            small
            layout="prev, pager, next"
            v-show="formTotal>0"
            :total="formTotal"
            :page.sync="formQueryParams.pageNum"
            :limit.sync="formQueryParams.pageSize"
            @pagination="ListFormDeploy"
          />
        </el-col>
        <el-col :span="14" :xs="24">
          <div class="test-form">
            <v-form-render :form-data="formData" ref="vFormCurrentRowRef"/>
          </div>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>
<script>
import MyPagination from "@/views/flowable/test/mypagination/index.vue";
import {addDeployment, delDeployment, listDefinition, updateDeployment, updateState} from "@/api/flowable/definition";
import {addDeployForm, listForm} from "@/api/flowable/form";

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
      // 挂载表单到流程实例
      formDeployParam:{
        formId: null,
        deployId: null
      },
      formQueryParams:{
        pageNum: 1,
        pageSize: 10,
      },
      formDeployOpen: false,
      formDeployTitle: "",
      formList: [],
      formTotal:0,

      formConfOpen: false,
      formTitle: "",
      formData: {}, // 默认表单数据
    }
  },
  created() {
    this.getList();
  },
  methods: {
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
    handleLoadXml(row) {
      this.$router.push({ path: '/flowable/test/model', query: {deployId: row.deploymentId }})
    },
    handleUpdateSuspensionState(row){
      let state = 1;
      if(row.suspensionState===1){
        state = 2
      }
      const params={
        deployId: row.deploymentId,
        state:state
      }
      updateState(params).then(res => {
        this.$modal.msgSuccess(res.msg);
        this.getList();
      });
    },
    handleDelete(row){
      const deploymentIds = row.deploymentId || this.ids;
      this.$confirm('是否确认删除流程定义编号为"' + deploymentIds + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return delDeployment(deploymentIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      })
    },
    handleQuery(){
      this.queryParams.pageNum = 1;
      this.getList();
    },
    reset(){
      this.resetForm('queryForm');
      this.handleQuery();
    },
    handleAddForm(row){
      this.formDeployParam.deployId = row.deploymentId;
      this.ListFormDeploy();
    },
    /** 挂载表单列表 */
    ListFormDeploy(){
      listForm(this.formQueryParams).then(res =>{
        this.formList = res.rows;
        this.formTotal = res.total;
        this.formDeployOpen = true;
        this.formDeployTitle = "挂载表单";
      })
    },
    handleCurrentChange(data) {
      if (data) {
        this.$nextTick(() => {
          // 回显数据
          this.$refs.vFormCurrentRowRef.setFormJson(JSON.parse(data.formContent))

            this.$refs.vFormCurrentRowRef.disableForm();

        })
      }
    },
    /** 挂载表单 */
    submitFormDeploy(row){
      this.formDeployParam.formId = row.formId;
      addDeployForm(this.formDeployParam).then(res =>{
        this.$modal.msgSuccess(res.msg);
        this.formDeployOpen = false;
        this.getList();
      })
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
