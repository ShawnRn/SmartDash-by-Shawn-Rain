$gwx0_XC_1 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx0_XC_1 || [];
    function gz$gwx0_XC_1_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx0_XC_1_1)
        return __WXML_GLOBAL__.ops_cached.$gwx0_XC_1_1;
      __WXML_GLOBAL__.ops_cached.$gwx0_XC_1_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx0_XC_1_1);
      return __WXML_GLOBAL__.ops_cached.$gwx0_XC_1_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx0_XC_1 = z;
    __WXML_GLOBAL__.ops_init.$gwx0_XC_1 = true;
    var x = ["./subpackages/contact/pages/maptest.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx0_XC_1_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx0_XC_1";
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
if (__vd_version_info__.delayedGwx || false) $gwx0_XC_1();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/contact/pages/maptest.wxml"] = [
    $gwx0_XC_1,
    "./subpackages/contact/pages/maptest.wxml",
  ];
else
  __wxAppCode__["subpackages/contact/pages/maptest.wxml"] = $gwx0_XC_1(
    "./subpackages/contact/pages/maptest.wxml"
  );
__wxRoute = "subpackages/contact/pages/maptest";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/contact/pages/maptest.js";
define(
  "subpackages/contact/pages/maptest.js",
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
      ["subpackages/contact/pages/maptest"],
      {
        121:
          /*!*********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Fcontact%2Fpages%2Fmaptest"} ***!
    \*********************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            (function (n, t) {
              var o = e(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              e(/*! uni-pages */ 26);
              o(e(/*! vue */ 25));
              var c = o(e(/*! ./subpackages/contact/pages/maptest.nvue */ 122));
              (n.__webpack_require_UNI_MP_PLUGIN__ = e), t(c.default);
            }).call(
              this,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        122:
          /*!*************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue ***!
    \*************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! ./maptest.nvue?vue&type=template&id=6dda16aa&scoped=true& */ 123
              ),
              c = e(/*! ./maptest.nvue?vue&type=script&lang=js& */ 125);
            for (var a in c)
              ["default"].indexOf(a) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return c[n];
                  });
                })(a);
            e(/*! ./maptest.nvue?vue&type=style&index=0&lang=css& */ 127),
              e(
                /*! ./maptest.nvue?vue&type=style&index=1&id=6dda16aa&scoped=true&lang=css& */ 129
              );
            var i = e(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              r = Object(i.default)(
                c.default,
                o.render,
                o.staticRenderFns,
                !1,
                null,
                "6dda16aa",
                null,
                !1,
                o.components,
                void 0
              );
            (r.options.__file = "subpackages/contact/pages/maptest.nvue"),
              (t.default = r.exports);
          },
        123:
          /*!********************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=template&id=6dda16aa&scoped=true& ***!
    \********************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, t, e) {
            e.r(t);
            var o = e(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./maptest.nvue?vue&type=template&id=6dda16aa&scoped=true& */ 124
            );
            e.d(t, "render", function () {
              return o.render;
            }),
              e.d(t, "staticRenderFns", function () {
                return o.staticRenderFns;
              }),
              e.d(t, "recyclableRender", function () {
                return o.recyclableRender;
              }),
              e.d(t, "components", function () {
                return o.components;
              });
          },
        124:
          /*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=template&id=6dda16aa&scoped=true& ***!
    \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, t, e) {
            e.r(t),
              e.d(t, "render", function () {
                return o;
              }),
              e.d(t, "staticRenderFns", function () {
                return a;
              }),
              e.d(t, "recyclableRender", function () {
                return c;
              }),
              e.d(t, "components", function () {});
            var o = function () {
                var n = this.$createElement,
                  t = (this._self._c, this.$t("contact.companyName")),
                  e = this.$t("contact.address"),
                  o = this.$t("contact.phone"),
                  c = this.$t("contact.button1"),
                  a = this.$t("contact.button2"),
                  i = this.$t("contact.button3");
                this.$mp.data = Object.assign(
                  {},
                  { $root: { m0: t, m1: e, m2: o, m3: c, m4: a, m5: i } }
                );
              },
              c = !1,
              a = [];
            o._withStripped = !0;
          },
        125:
          /*!**************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=script&lang=js& ***!
    \**************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./maptest.nvue?vue&type=script&lang=js& */ 126
              ),
              c = e.n(o);
            for (var a in o)
              ["default"].indexOf(a) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(a);
            t.default = c.a;
          },
        126:
          /*!*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=script&lang=js& ***!
    \*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            (function (n) {
              Object.defineProperty(t, "__esModule", { value: !0 }),
                (t.default = void 0);
              var o = e(/*! @/utils/i18n.js */ 40),
                c = {
                  computed: {
                    $t: function () {
                      return o.t;
                    },
                  },
                  data: function () {
                    return {
                      longitude: 120.42279277979279,
                      latitude: 31.292713854661812,
                      scale: 16,
                      markers: [
                        {
                          id: 0,
                          longitude: 120.41779277979279,
                          latitude: 31.292713854661812,
                          title: "智科",
                          anchor: { x: 1, y: 1 },
                          width: 30,
                          height: 30,
                          callout: {
                            content: "智科",
                            color: "#FFFFFF",
                            fontSize: 14,
                            borderRadius: 5,
                            bgColor: "#000000",
                            padding: 8,
                            display: "ALWAYS",
                          },
                        },
                      ],
                    };
                  },
                  methods: {
                    page_click_handle: function (t) {
                      switch (t) {
                        case 0:
                          n.showModal({
                            title: "寄件注意事项",
                            content:
                              "寄件时请将故障情况以及收件人地址和电话写到小纸条上一并寄过来，中间涉及到费用时会给您打电话确认。",
                            showCancel: !1,
                            confirmText: "确定",
                            confirmColor: "#3CC51F",
                            success: function (n) {
                              n.confirm
                                ? console.log("用户点击了确定")
                                : n.cancel && console.log("用户点击了取消");
                            },
                            fail: function (n) {
                              console.log(n);
                            },
                          });
                          break;
                        case 1:
                          n.showModal({
                            title: "工作时间",
                            content:
                              "业务咨询请拨打座机，售后相关请拨打手机号，工作时间：法定工作日8：00--16:30",
                            showCancel: !1,
                            confirmText: "确定",
                            confirmColor: "#3CC51F",
                            success: function (n) {
                              n.confirm
                                ? console.log("用户点击了确定")
                                : n.cancel && console.log("用户点击了取消");
                            },
                            fail: function (n) {
                              console.log(n);
                            },
                          });
                          break;
                        case 2:
                          n.navigateTo({ url: "/subpackages/contact/query" });
                      }
                    },
                    jumpToPolicy: function (t) {
                      console.log("12"),
                        "privacyPolicy" === t
                          ? n.navigateTo({
                              url: "/pages/webview/webview?url=".concat(
                                encodeURIComponent(
                                  "https://www.gekoodriver.com/?policy"
                                )
                              ),
                            })
                          : "userAgreement" === t &&
                            n.navigateTo({
                              url: "/pages/webview/webview?url=".concat(
                                encodeURIComponent(
                                  "https://www.gekoodriver.com/?useragreement"
                                )
                              ),
                            });
                    },
                    jumpToBeian: function () {
                      n.navigateTo({
                        url: "/pages/webview/webview?url=".concat(
                          encodeURIComponent(
                            "https://beian.miit.gov.cn/#/Integrated/index"
                          )
                        ),
                      });
                    },
                  },
                  onLoad: function () {
                    n.setNavigationBarTitle({
                      title: this.$t("home_page.grid.item2"),
                    });
                  },
                };
              t.default = c;
            }).call(
              this,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        127:
          /*!**********************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=style&index=0&lang=css& ***!
    \**********************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./maptest.nvue?vue&type=style&index=0&lang=css& */ 128
              ),
              c = e.n(o);
            for (var a in o)
              ["default"].indexOf(a) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(a);
            t.default = c.a;
          },
        128:
          /*!**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=style&index=0&lang=css& ***!
    \**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {},
        129:
          /*!**********************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=style&index=1&id=6dda16aa&scoped=true&lang=css& ***!
    \**********************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./maptest.nvue?vue&type=style&index=1&id=6dda16aa&scoped=true&lang=css& */ 130
              ),
              c = e.n(o);
            for (var a in o)
              ["default"].indexOf(a) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(a);
            t.default = c.a;
          },
        130:
          /*!**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/maptest.nvue?vue&type=style&index=1&id=6dda16aa&scoped=true&lang=css& ***!
    \**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {},
      },
      [[121, "common/runtime", "common/vendor"]],
    ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/contact/pages/maptest.js",
  }
);
require("subpackages/contact/pages/maptest.js");
