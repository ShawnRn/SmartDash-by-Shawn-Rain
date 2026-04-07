$gwx1_XC_0 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx1_XC_0 || [];
    function gz$gwx1_XC_0_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx1_XC_0_1)
        return __WXML_GLOBAL__.ops_cached.$gwx1_XC_0_1;
      __WXML_GLOBAL__.ops_cached.$gwx1_XC_0_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx1_XC_0_1);
      return __WXML_GLOBAL__.ops_cached.$gwx1_XC_0_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx1_XC_0 = z;
    __WXML_GLOBAL__.ops_init.$gwx1_XC_0 = true;
    var x = ["./subpackages/product/pages/description.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx1_XC_0_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx1_XC_0";
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
if (__vd_version_info__.delayedGwx || false) $gwx1_XC_0();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/product/pages/description.wxml"] = [
    $gwx1_XC_0,
    "./subpackages/product/pages/description.wxml",
  ];
else
  __wxAppCode__["subpackages/product/pages/description.wxml"] = $gwx1_XC_0(
    "./subpackages/product/pages/description.wxml"
  );
__wxRoute = "subpackages/product/pages/description";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/product/pages/description.js";
define(
  "subpackages/product/pages/description.js",
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
      ["subpackages/product/pages/description"],
      {
        147:
          /*!*************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Fproduct%2Fpages%2Fdescription"} ***!
    \*************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            (function (n, e) {
              var i = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              t(/*! uni-pages */ 26);
              i(t(/*! vue */ 25));
              var o = i(
                t(/*! ./subpackages/product/pages/description.vue */ 148)
              );
              (n.__webpack_require_UNI_MP_PLUGIN__ = t), e(o.default);
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        148:
          /*!****************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue ***!
    \****************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var i = t(
                /*! ./description.vue?vue&type=template&id=c1ee6ede&scoped=true& */ 149
              ),
              o = t(/*! ./description.vue?vue&type=script&lang=js& */ 151);
            for (var c in o)
              ["default"].indexOf(c) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return o[n];
                  });
                })(c);
            t(
              /*! ./description.vue?vue&type=style&index=0&id=c1ee6ede&scoped=true&lang=css& */ 153
            );
            var r = t(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              u = Object(r.default)(
                o.default,
                i.render,
                i.staticRenderFns,
                !1,
                null,
                "c1ee6ede",
                null,
                !1,
                i.components,
                void 0
              );
            (u.options.__file = "subpackages/product/pages/description.vue"),
              (e.default = u.exports);
          },
        149:
          /*!***********************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue?vue&type=template&id=c1ee6ede&scoped=true& ***!
    \***********************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, e, t) {
            t.r(e);
            var i = t(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./description.vue?vue&type=template&id=c1ee6ede&scoped=true& */ 150
            );
            t.d(e, "render", function () {
              return i.render;
            }),
              t.d(e, "staticRenderFns", function () {
                return i.staticRenderFns;
              }),
              t.d(e, "recyclableRender", function () {
                return i.recyclableRender;
              }),
              t.d(e, "components", function () {
                return i.components;
              });
          },
        150:
          /*!***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue?vue&type=template&id=c1ee6ede&scoped=true& ***!
    \***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, e, t) {
            t.r(e),
              t.d(e, "render", function () {
                return i;
              }),
              t.d(e, "staticRenderFns", function () {
                return c;
              }),
              t.d(e, "recyclableRender", function () {
                return o;
              }),
              t.d(e, "components", function () {});
            var i = function () {
                var n = this.$createElement;
                this._self._c;
              },
              o = !1,
              c = [];
            i._withStripped = !0;
          },
        151:
          /*!*****************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue?vue&type=script&lang=js& ***!
    \*****************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var i = t(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./description.vue?vue&type=script&lang=js& */ 152
              ),
              o = t.n(i);
            for (var c in i)
              ["default"].indexOf(c) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return i[n];
                  });
                })(c);
            e.default = o.a;
          },
        152:
          /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue?vue&type=script&lang=js& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            (function (n) {
              Object.defineProperty(e, "__esModule", { value: !0 }),
                (e.default = void 0);
              var i = t(/*! @/utils/i18n.js */ 40),
                o = {
                  computed: {
                    $t: function () {
                      return i.t;
                    },
                  },
                  data: function () {
                    return {
                      itemList: [
                        {
                          iconClass: ".cuIcon-selection",
                          title: "控制器阵脚定义",
                          desc: "各系列的控制器的阵脚定义",
                          link: "https://mp.weixin.qq.com/s/9pwKRH2zBL8PonZYOrhpjA?token=1294366477&lang=zh_CN",
                        },
                        {
                          iconClass: ".cuIcon-selection",
                          title: "自学习过程",
                          desc: "主要讲解控制器自学习的过程和注意事项",
                          link: "https://mp.weixin.qq.com/s/JlmJMnAYh0VlIHISOkxXOA?token=1294366477&lang=zh_CN",
                          dy_link: "https://v.douyin.com/iPf1YJhn/",
                        },
                        {
                          iconClass: ".cuIcon-selection",
                          title: "调参演示",
                          desc: "实车讲解调参步骤和过程",
                          link: "https://mp.weixin.qq.com/s/KPM6kmiP_ckcwN5rV7eG4w?token=1294366477&lang=zh_CN",
                          dy_link: "https://v.douyin.com/iPfJdVry/",
                        },
                        {
                          iconClass: ".cuIcon-selection",
                          title: "弱磁设置方法",
                          desc: "讲解弱磁的匹配方法",
                          link: "https://mp.weixin.qq.com/s/FLxojsDIpdenjPhL7UErqQ?token=1294366477&lang=zh_CN",
                          dy_link: "https://v.douyin.com/iPfJxyuW/",
                        },
                        {
                          iconClass: ".cuIcon-selection",
                          title: "固件升级",
                          desc: "讲解通过蓝牙升级的方法和出错后的补救方法",
                          link: "https://mp.weixin.qq.com/s/4d7HJbA9_OqkglsgZO0bbw?token=1294366477&lang=zh_CN",
                          dy_link:
                            "https://www.douyin.com/video/7374064673827720467",
                        },
                      ],
                    };
                  },
                  methods: {
                    openLink: function (e, t) {
                      n.navigateTo({
                        url: "/pages/webview/webview?url=".concat(
                          encodeURIComponent(e)
                        ),
                      });
                    },
                  },
                  onLoad: function () {
                    n.setNavigationBarTitle({
                      title: this.$t("home_page.grid.item1"),
                    });
                  },
                };
              e.default = o;
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        153:
          /*!*************************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue?vue&type=style&index=0&id=c1ee6ede&scoped=true&lang=css& ***!
    \*************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var i = t(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./description.vue?vue&type=style&index=0&id=c1ee6ede&scoped=true&lang=css& */ 154
              ),
              o = t.n(i);
            for (var c in i)
              ["default"].indexOf(c) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return i[n];
                  });
                })(c);
            e.default = o.a;
          },
        154:
          /*!*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/description.vue?vue&type=style&index=0&id=c1ee6ede&scoped=true&lang=css& ***!
    \*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {},
      },
      [[147, "common/runtime", "common/vendor"]],
    ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/product/pages/description.js",
  }
);
require("subpackages/product/pages/description.js");
