$gwx_XC_3 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_3 || [];
    function gz$gwx_XC_3_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_3_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_3_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_3_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_3_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_3_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_3 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_3 = true;
    var x = ["./pages/language/language.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_3_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_3";
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
if (__vd_version_info__.delayedGwx || false) $gwx_XC_3();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["pages/language/language.wxml"] = [
    $gwx_XC_3,
    "./pages/language/language.wxml",
  ];
else
  __wxAppCode__["pages/language/language.wxml"] = $gwx_XC_3(
    "./pages/language/language.wxml"
  );
__wxRoute = "pages/language/language";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "pages/language/language.js";
define(
  "pages/language/language.js",
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
      ["pages/language/language"],
      {
        81:
          /*!*********************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"pages%2Flanguage%2Flanguage"} ***!
    \*********************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            (function (e, n) {
              var o = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              t(/*! uni-pages */ 26);
              o(t(/*! vue */ 25));
              var a = o(t(/*! ./pages/language/language.vue */ 82));
              (e.__webpack_require_UNI_MP_PLUGIN__ = t), n(a.default);
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        82:
          /*!**************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue ***!
    \**************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            t.r(n);
            var o = t(
                /*! ./language.vue?vue&type=template&id=202eccf4&name=basics& */ 83
              ),
              a = t(/*! ./language.vue?vue&type=script&lang=js& */ 85);
            for (var c in a)
              ["default"].indexOf(c) < 0 &&
                (function (e) {
                  t.d(n, e, function () {
                    return a[e];
                  });
                })(c);
            t(/*! ./language.vue?vue&type=style&index=0&lang=css& */ 87);
            var r = t(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              i = Object(r.default)(
                a.default,
                o.render,
                o.staticRenderFns,
                !1,
                null,
                null,
                null,
                !1,
                o.components,
                void 0
              );
            (i.options.__file = "pages/language/language.vue"),
              (n.default = i.exports);
          },
        83:
          /*!*********************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue?vue&type=template&id=202eccf4&name=basics& ***!
    \*********************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (e, n, t) {
            t.r(n);
            var o = t(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./language.vue?vue&type=template&id=202eccf4&name=basics& */ 84
            );
            t.d(n, "render", function () {
              return o.render;
            }),
              t.d(n, "staticRenderFns", function () {
                return o.staticRenderFns;
              }),
              t.d(n, "recyclableRender", function () {
                return o.recyclableRender;
              }),
              t.d(n, "components", function () {
                return o.components;
              });
          },
        84:
          /*!*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue?vue&type=template&id=202eccf4&name=basics& ***!
    \*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (e, n, t) {
            t.r(n),
              t.d(n, "render", function () {
                return o;
              }),
              t.d(n, "staticRenderFns", function () {
                return c;
              }),
              t.d(n, "recyclableRender", function () {
                return a;
              }),
              t.d(n, "components", function () {});
            var o = function () {
                var e = this.$createElement,
                  n = (this._self._c, this.$t("lang_page.tip"));
                this.$mp.data = Object.assign({}, { $root: { m0: n } });
              },
              a = !1,
              c = [];
            o._withStripped = !0;
          },
        85:
          /*!***************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue?vue&type=script&lang=js& ***!
    \***************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            t.r(n);
            var o = t(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./language.vue?vue&type=script&lang=js& */ 86
              ),
              a = t.n(o);
            for (var c in o)
              ["default"].indexOf(c) < 0 &&
                (function (e) {
                  t.d(n, e, function () {
                    return o[e];
                  });
                })(c);
            n.default = a.a;
          },
        86:
          /*!**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue?vue&type=script&lang=js& ***!
    \**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            (function (e) {
              Object.defineProperty(n, "__esModule", { value: !0 }),
                (n.default = void 0);
              var o = t(/*! @/utils/i18n.js */ 40),
                a = {
                  name: "basics",
                  computed: {
                    $t: function () {
                      return o.t;
                    },
                  },
                  data: function () {
                    return {
                      elements: [
                        {
                          title: "中文",
                          name: "Chinese",
                          color: "cyan",
                          cuIcon: "evaluate",
                        },
                        {
                          title: "英语",
                          name: "English",
                          color: "blue",
                          cuIcon: "hot",
                        },
                        {
                          title: "繁体",
                          name: "Unsimplified",
                          color: "cyan",
                          cuIcon: "evaluate",
                        },
                        {
                          title: "日语",
                          name: "Japanese",
                          color: "blue",
                          cuIcon: "hot",
                        },
                      ],
                    };
                  },
                  methods: {
                    change_language_event: function (n) {
                      switch (n) {
                        case 0:
                          (0, o.setLocale)("zh"), console.log("zh");
                          break;
                        case 1:
                          (0, o.setLocale)("en");
                          break;
                        case 2:
                          (0, o.setLocale)("zhft");
                          break;
                        case 3:
                          return void e.showToast({
                            title: "敬请期待",
                            icon: "exception",
                            duration: 2e3,
                          });
                      }
                      this.$forceUpdate(),
                        e.reLaunch({
                          url: "/pages/index/index",
                          success: function () {
                            console.log("小程序已重启");
                          },
                          fail: function (e) {
                            console.error("重启失败:", e);
                          },
                        });
                    },
                  },
                  onShow: function () {
                    console.log("success");
                  },
                  onLoad: function () {
                    e.setNavigationBarTitle({
                      title: this.$t("home_page.grid.item7"),
                    });
                  },
                };
              n.default = a;
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        87:
          /*!***********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue?vue&type=style&index=0&lang=css& ***!
    \***********************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            t.r(n);
            var o = t(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./language.vue?vue&type=style&index=0&lang=css& */ 88
              ),
              a = t.n(o);
            for (var c in o)
              ["default"].indexOf(c) < 0 &&
                (function (e) {
                  t.d(n, e, function () {
                    return o[e];
                  });
                })(c);
            n.default = a.a;
          },
        88:
          /*!***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/language/language.vue?vue&type=style&index=0&lang=css& ***!
    \***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {},
      },
      [[81, "common/runtime", "common/vendor"]],
    ]);
  },
  { isPage: true, isComponent: true, currentFile: "pages/language/language.js" }
);
require("pages/language/language.js");
