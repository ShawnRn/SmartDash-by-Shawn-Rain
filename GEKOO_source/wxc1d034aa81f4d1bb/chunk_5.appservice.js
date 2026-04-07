$gwx_XC_5 = (function (
  _,
  _v,
  _n,
  _p,
  _s,
  _wp,
  _wl,
  $gwn,
  $gwl,
  $gwh,
  wh,
  $gstack,
  $gwrt,
  gra,
  grb,
  TestTest,
  wfor,
  _ca,
  _da,
  _r,
  _rz,
  _o,
  _oz,
  _1,
  _1z,
  _2,
  _2z,
  _m,
  _mz,
  nv_getDate,
  nv_getRegExp,
  nv_console,
  nv_parseInt,
  nv_parseFloat,
  nv_isNaN,
  nv_isFinite,
  nv_decodeURI,
  nv_decodeURIComponent,
  nv_encodeURI,
  nv_encodeURIComponent,
  $gdc,
  nv_JSON,
  _af,
  _gv,
  _ai,
  _grp,
  _gd,
  _gapi,
  $ixc,
  _ic,
  _w,
  _ev,
  _tsd
) {
  return function (path, global) {
    if (typeof global === "undefined") {
      if (typeof __GWX_GLOBAL__ === "undefined") global = {};
      else global = __GWX_GLOBAL__;
    }
    if (typeof __WXML_GLOBAL__ === "undefined") {
      __WXML_GLOBAL__ = {};
    }
    __WXML_GLOBAL__.modules = __WXML_GLOBAL__.modules || {};
    var e_ = {};
    if (typeof global.entrys === "undefined") global.entrys = {};
    e_ = global.entrys;
    var d_ = {};
    if (typeof global.defines === "undefined") global.defines = {};
    d_ = global.defines;
    var f_ = {};
    if (typeof global.modules === "undefined") global.modules = {};
    f_ = global.modules || {};
    var p_ = {};
    __WXML_GLOBAL__.ops_cached = __WXML_GLOBAL__.ops_cached || {};
    __WXML_GLOBAL__.ops_set = __WXML_GLOBAL__.ops_set || {};
    __WXML_GLOBAL__.ops_init = __WXML_GLOBAL__.ops_init || {};
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_5 || [];
    function gz$gwx_XC_5_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_5_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_5_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_5_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_5_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_5_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_5 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_5 = true;
    var x = ["./pages/webview/webview.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_5_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_5";
        var main = e_[path].f;
        if (typeof global === "undefined") global = {};
        global.f = $gdc(f_[path], "", 1);
        try {
          main(env, {}, root, global);
          _tsd(root);
        } catch (err) {
          console.log(err);
        }
        g = "";
        return root;
      };
    }
  };
})(
  __g.a,
  __g.b,
  __g.c,
  __g.d,
  __g.e,
  __g.f,
  __g.g,
  __g.h,
  __g.i,
  __g.j,
  __g.k,
  __g.l,
  __g.m,
  __g.n,
  __g.o,
  __g.p,
  __g.q,
  __g.r,
  __g.s,
  __g.t,
  __g.u,
  __g.v,
  __g.w,
  __g.x,
  __g.y,
  __g.z,
  __g.A,
  __g.B,
  __g.C,
  __g.D,
  __g.E,
  __g.F,
  __g.G,
  __g.H,
  __g.I,
  __g.J,
  __g.K,
  __g.L,
  __g.M,
  __g.N,
  __g.O,
  __g.P,
  __g.Q,
  __g.R,
  __g.S,
  __g.T,
  __g.U,
  __g.V,
  __g.W,
  __g.X,
  __g.Y,
  __g.Z,
  __g.aa
);
if (__vd_version_info__.delayedGwx || false) $gwx_XC_5();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["pages/webview/webview.wxml"] = [
    $gwx_XC_5,
    "./pages/webview/webview.wxml",
  ];
else
  __wxAppCode__["pages/webview/webview.wxml"] = $gwx_XC_5(
    "./pages/webview/webview.wxml"
  );
__wxRoute = "pages/webview/webview";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "pages/webview/webview.js";
define(
  "pages/webview/webview.js",
  function (
    require,
    module,
    exports,
    window,
    document,
    frames,
    self,
    location,
    navigator,
    localStorage,
    history,
    Caches,
    screen,
    alert,
    confirm,
    prompt,
    XMLHttpRequest,
    WebSocket,
    Reporter,
    webkit,
    WeixinJSCore
  ) {
    "use strict";
    (global.webpackJsonp = global.webpackJsonp || []).push([
      ["pages/webview/webview"],
      {
        89:
          /*!*******************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"pages%2Fwebview%2Fwebview"} ***!
    \*******************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            (function (n, e) {
              var r = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              t(/*! uni-pages */ 26);
              r(t(/*! vue */ 25));
              var u = r(t(/*! ./pages/webview/webview.vue */ 90));
              (n.__webpack_require_UNI_MP_PLUGIN__ = t), e(u.default);
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        90:
          /*!************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue ***!
    \************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var r = t(
                /*! ./webview.vue?vue&type=template&id=baadaa0c&scoped=true& */ 91
              ),
              u = t(/*! ./webview.vue?vue&type=script&lang=js& */ 93);
            for (var o in u)
              ["default"].indexOf(o) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return u[n];
                  });
                })(o);
            t(
              /*! ./webview.vue?vue&type=style&index=0&id=baadaa0c&scoped=true&lang=css& */ 95
            );
            var c = t(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              i = Object(c.default)(
                u.default,
                r.render,
                r.staticRenderFns,
                !1,
                null,
                "baadaa0c",
                null,
                !1,
                r.components,
                void 0
              );
            (i.options.__file = "pages/webview/webview.vue"),
              (e.default = i.exports);
          },
        91:
          /*!*******************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue?vue&type=template&id=baadaa0c&scoped=true& ***!
    \*******************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, e, t) {
            t.r(e);
            var r = t(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./webview.vue?vue&type=template&id=baadaa0c&scoped=true& */ 92
            );
            t.d(e, "render", function () {
              return r.render;
            }),
              t.d(e, "staticRenderFns", function () {
                return r.staticRenderFns;
              }),
              t.d(e, "recyclableRender", function () {
                return r.recyclableRender;
              }),
              t.d(e, "components", function () {
                return r.components;
              });
          },
        92:
          /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue?vue&type=template&id=baadaa0c&scoped=true& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, e, t) {
            t.r(e),
              t.d(e, "render", function () {
                return r;
              }),
              t.d(e, "staticRenderFns", function () {
                return o;
              }),
              t.d(e, "recyclableRender", function () {
                return u;
              }),
              t.d(e, "components", function () {});
            var r = function () {
                var n = this.$createElement;
                this._self._c;
              },
              u = !1,
              o = [];
            r._withStripped = !0;
          },
        93:
          /*!*************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue?vue&type=script&lang=js& ***!
    \*************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var r = t(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./webview.vue?vue&type=script&lang=js& */ 94
              ),
              u = t.n(r);
            for (var o in r)
              ["default"].indexOf(o) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return r[n];
                  });
                })(o);
            e.default = u.a;
          },
        94:
          /*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue?vue&type=script&lang=js& ***!
    \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            Object.defineProperty(e, "__esModule", { value: !0 }),
              (e.default = void 0);
            var r = {
              data: function () {
                return { url: "" };
              },
              onLoad: function (n) {
                this.url = decodeURIComponent(n.url);
              },
            };
            e.default = r;
          },
        95:
          /*!*********************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue?vue&type=style&index=0&id=baadaa0c&scoped=true&lang=css& ***!
    \*********************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var r = t(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./webview.vue?vue&type=style&index=0&id=baadaa0c&scoped=true&lang=css& */ 96
              ),
              u = t.n(r);
            for (var o in r)
              ["default"].indexOf(o) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return r[n];
                  });
                })(o);
            e.default = u.a;
          },
        96:
          /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/webview/webview.vue?vue&type=style&index=0&id=baadaa0c&scoped=true&lang=css& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {},
      },
      [[89, "common/runtime", "common/vendor"]],
    ]);
  },
  { isPage: true, isComponent: true, currentFile: "pages/webview/webview.js" }
);
require("pages/webview/webview.js");
