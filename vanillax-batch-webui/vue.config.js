module.exports = {
  lintOnSave: false,
  runtimeCompiler: true,
  devServer: {
    open: process.platform === 'darwin',
    host: '0.0.0.0',
    port: 3000, // CHANGE YOUR PORT HERE!
    https: false,
    hotOnly: false,
    proxy: {
      '^/rest': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      },
    }
  },

}
