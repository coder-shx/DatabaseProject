const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },
  // 生产环境是否生成 sourceMap 文件
  productionSourceMap: false,
  // 可选，启用并配置 CSS 相关选项
  css: {
    // 是否将组件中的 CSS 提取至一个独立的 CSS 文件中
    extract: process.env.NODE_ENV === 'production',
    // 是否为 CSS 开启 source map
    sourceMap: false,
    // 为预处理器的 loader 传递选项
    loaderOptions: {
      // 给 sass-loader 传递选项
      sass: {
        // 自动导入全局样式文件
        additionalData: `
          @import "@/assets/styles/variables.scss";
        `
      }
    }
  }
})
