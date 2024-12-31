<template>
  <div class="mycontainer">
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes="pageSizes"
      :layout="layout"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
    >
      <!--  currentPage和  pageSize是用户点击子组件改变的  -->
      <!--  total是父组件查询计算得出的  -->
    </el-pagination>
  </div>
</template>

<script>
import {scrollTo} from "@/utils/scroll-to";

export default {
  name: 'MyPagination',
  props: {
    total: {
      required: true,
      type: Number
    },
    layout: {
      type: String,
      default: 'total, sizes, prev, pager, next, jumper'
    },
    pageSizes: {
      type: Array,
      default() {
        return [10, 20, 30, 50]
      }
    },
    page: {
      type: Number,
      default: 1
    },
  },
  data() {
    return {
      currentPage: 1,
      pageSize: 10
    };
  },
  computed: {
  },
  methods: {
    handleSizeChange(val) {
      if (this.currentPage * val > this.total) {
        this.currentPage = 1
      }
      this.$emit('pagination', { pageNum: this.currentPage, pageSize: val })
      /*if (this.autoScroll) {
        scrollTo(0, 800)
      }*/
    },
    handleCurrentChange(val) {
      this.$emit('pagination', { pageNum: val, pageSize: this.pageSize })

    }
  }
}
</script>

<style scoped>
.pagination-container {
  background: #fff;
  padding: 32px 16px;
}
.pagination-container.hidden {
  display: none;
}
.mycontainer {
  background: #fff;
  padding: 32px 16px;
  right: 0;
  position: absolute;
}
</style>
