<template>
  <div v-loading="isView" class="flow-containers" :class="{ 'view-mode': isView }">
    <div style="height: 100%">

      <el-container>
        <el-header style="border-bottom: solid 1px rgb(218 218 218);height: auto;padding-left: 0">
          <div style="display:flex;justify-content: space-between;padding: 5px">
            <el-button-group>
              <el-button size="mini" icon="el-icon-circle-plus" @click="newDiagram"/>
              <el-button size="mini" icon="el-icon-rank" @click="fitViewport"/>
              <el-button size="mini" icon="el-icon-zoom-in" @click="zoomViewport(true)"/>
              <el-button size="mini" icon="el-icon-zoom-out" @click="zoomViewport(false)"/>
              <el-button size="mini" icon="el-icon-back" @click="modeler.get('commandStack').undo()"/>
              <el-button size="mini" icon="el-icon-right" @click="modeler.get('commandStack').redo()"/>
            </el-button-group>
            <el-button-group>
              <el-button size="mini" icon="el-icon-view" @click="showXML">查看xml</el-button>
              <el-button size="mini" icon="el-icon-download" @click="saveXML(true)">下载xml</el-button>
              <el-button size="mini" icon="el-icon-picture" @click="saveImg('svg', true)">下载svg</el-button>
              <el-button size="mini" type="primary" @click="save">保存模型</el-button>
              <el-button size="mini" type="danger" @click="goBack">关闭</el-button>
            </el-button-group>
          </div>
        </el-header>
        <el-container>
          <el-aside width="80%">
            <div ref="canvas" class="canvas"/>
          </el-aside>
          <el-main>
            <designer v-if="loadCanvas"></designer>
          </el-main>
        </el-container>
      </el-container>


    </div>
  </div>
</template>

<script>
// 汉化
import customTranslate from './customPanel/customTranslate'
import Modeler from 'bpmn-js/lib/Modeler'
import Designer from './designer'
import getInitStr from './flowable/init'
import {StrUtil} from '@/utils/StrUtil'
// 引入flowable的节点文件
import FlowableModule from './flowable/flowable.json'
import customControlsModule from './customPanel'

export default {
  name: "MyBpmnModel",
  components: {Designer},
  /** 组件传值  */
  props: {
    xml: {
      type: String,
      default: ''
    },
    isView: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      modeler: null,
      zoom: 1,
      loadCanvas: false,  // 当前组件渲染然后再加载canvas
      simulationStatus: false,
      bpmnlintStatus: false,
      simulation: true,
      designer: true,
    }
  },
  /** 传值监听 */
  watch: {
    xml: {
      handler(newVal) {
        if (StrUtil.isNotBlank(newVal)) {
          this.createNewDiagram(newVal)
        } else {
          this.newDiagram()
        }
      },
      immediate: true, // 立即生效
    },
  },
  computed: {
    additionalModules() {
      const Modules = [];
      Modules.push(customControlsModule);
      Modules.push({ //汉化
        translate: ['value', customTranslate]
      });
      return Modules;
    },
  },
  mounted() {
    const modeler = new Modeler({
      container: this.$refs.canvas,
      additionalModules: this.additionalModules,
      moddleExtensions: {
        flowable: FlowableModule
      },
      keyboard: {bindTo: document}
    })
    this.modeler = modeler;
    this.modelerStore.modeler = modeler;
    this.modelerStore.modeling = modeler.get("modeling");
    this.modelerStore.moddle = modeler.get("moddle");
    this.modelerStore.canvas = modeler.get("canvas");
    this.modelerStore.bpmnFactory = modeler.get("bpmnFactory");
    this.modelerStore.elRegistry = modeler.get("elementRegistry");
    if (StrUtil.isBlank(this.xml)) {
      this.newDiagram();
    } else {
      this.createNewDiagram(this.xml);
    }
  },
  methods: {
    // 根据默认文件初始化流程图
    newDiagram() {
      this.createNewDiagram(getInitStr())
    },

    async createNewDiagram(data) {
      if (StrUtil.isNotBlank(this.modelerStore.modeler)) {
        data = data.replace(/<!\[CDATA\[(.+?)]]>/g, function (match, str) {
            return str.replace(/</g, '&lt;')
          }
        )
        try {
          await this.modelerStore.modeler.importXML(data);
          this.fitViewport()
        } catch (err) {
          console.error(err.message, err.warnings)
        }
      }
    },
    // 让图能自适应屏幕
    fitViewport() {
      const bbox = document.querySelector('.flow-containers .viewport').getBBox();
      const currentViewBox = this.modelerStore.canvas.viewbox()
      const elementMid = {
        x: bbox.x + bbox.width / 2 - 65,
        y: bbox.y + bbox.height / 2
      }
      const center = {
        x: elementMid.x - currentViewBox.width / 2,
        y: elementMid.y - currentViewBox.height / 2,
        width: currentViewBox.width,
        height: currentViewBox.height
      }
      this.modelerStore.canvas.viewbox(center)
      this.loadCanvas = true;
    },


    // 放大缩小
    zoomViewport(zoomIn = true) {
      this.zoom = this.modelerStore.canvas.zoom()
      this.zoom += (zoomIn ? 0.1 : -0.1)
      this.modelerStore.canvas.zoom(this.zoom)
    },

    // 获取流程基础信息
    getProcess() {
      const element = this.getProcessElement()
      return {
        id: element.id,
        name: element.name,
        category: element.processCategory
      }
    },

    // 获取流程主面板节点
    getProcessElement() {
      const rootElements = this.modelerStore.modeler.getDefinitions().rootElements
      for (let i = 0; i < rootElements.length; i++) {
        if (rootElements[i].$type === 'bpmn:Process') return rootElements[i]
      }
    },

    // 保存xml
    async saveXML(download = false) {
      try {
        const {xml} = await this.modelerStore.modeler.saveXML({format: true})
        if (download) {
          this.downloadFile(`${this.getProcessElement().name}.bpmn20.xml`, xml, 'application/xml')
        }
        return xml
      } catch (err) {
        console.log(err)
      }
    },

    // 在线查看xml
    async showXML() {
      try {
        const xmlStr = await this.saveXML()
        this.$emit('showXML', xmlStr)
      } catch (err) {
        console.log(err)
      }
    },

    // 保存流程图为svg
    async saveImg(type = 'svg', download = false) {
      try {
        const {svg} = await this.modelerStore.modeler.saveSVG({format: true})
        if (download) {
          this.downloadFile(this.getProcessElement().name, svg, 'image/svg+xml')
        }
        return svg
      } catch (err) {
        console.log(err)
      }
    },

    // 保存流程图
    async save() {
      const process = this.getProcess()
      const xml = await this.saveXML()
      const svg = await this.saveImg()
      const result = {process, xml, svg}
      this.$emit('save', result)
      window.parent.postMessage(result, '*')
      this.goBack();
    },

    // 打开流程文件
    openBpmn(file) {
      const reader = new FileReader()
      reader.readAsText(file, 'utf-8')
      reader.onload = () => {
        this.createNewDiagram(reader.result)
      }
      return false
    },

    // 下载流程文件
    downloadFile(filename, data, type) {
      const a = document.createElement('a');
      const url = window.URL.createObjectURL(new Blob([data], {type: type}));
      a.href = url
      a.download = filename
      a.click()
      window.URL.revokeObjectURL(url)
    },

    /** 关闭当前标签页并返回上个页面 */
    goBack() {
      const obj = {path: "/flowable/definition", query: {t: Date.now()}};
      this.$tab.closeOpenPage(obj);
      this.toggleSideBar();
    },
  }
}
</script>

<style lang="scss">
/*左边工具栏以及编辑节点的样式*/
@import "~bpmn-js/dist/assets/diagram-js.css";
@import "~bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
@import "~bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css";
@import "~bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css";
//@import "~bpmn-js-bpmnlint/dist/assets/css/bpmn-js-bpmnlint.css";
.flow-containers {
  width: 100%;
  height: 100%;}
</style>
