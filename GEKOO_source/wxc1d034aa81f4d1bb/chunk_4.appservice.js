$gwx_XC_4 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_4 || [];
    function gz$gwx_XC_4_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_4_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_4_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_4_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([[7], [3, "showTransition"]]);
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_4_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_4_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_4 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_4 = true;
    var x = ["./pages/logo/logo.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_4_1();
      var oVC = _v();
      _(r, oVC);
      if (_oz(z, 0, e, s, gg)) {
        oVC.wxVkey = 1;
      }
      oVC.wxXCkey = 1;
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_4";
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
if (__vd_version_info__.delayedGwx || false) $gwx_XC_4();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["pages/logo/logo.wxml"] = [$gwx_XC_4, "./pages/logo/logo.wxml"];
else
  __wxAppCode__["pages/logo/logo.wxml"] = $gwx_XC_4("./pages/logo/logo.wxml");
__wxRoute = "pages/logo/logo";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "pages/logo/logo.js";
define(
  "pages/logo/logo.js",
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
      ["pages/logo/logo"],
      {
        34:
          /*!*************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"pages%2Flogo%2Flogo"} ***!
    \*************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            (function (n, t) {
              var o = e(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              e(/*! uni-pages */ 26);
              o(e(/*! vue */ 25));
              var r = o(e(/*! ./pages/logo/logo.vue */ 35));
              (n.__webpack_require_UNI_MP_PLUGIN__ = e), t(r.default);
            }).call(
              this,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        35:
          /*!******************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue ***!
    \******************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! ./logo.vue?vue&type=template&id=140517e6&scoped=true& */ 36
              ),
              r = e(/*! ./logo.vue?vue&type=script&lang=js& */ 38);
            for (var i in r)
              ["default"].indexOf(i) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return r[n];
                  });
                })(i);
            e(
              /*! ./logo.vue?vue&type=style&index=0&id=140517e6&scoped=true&lang=css& */ 45
            );
            var u = e(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              a = Object(u.default)(
                r.default,
                o.render,
                o.staticRenderFns,
                !1,
                null,
                "140517e6",
                null,
                !1,
                o.components,
                void 0
              );
            (a.options.__file = "pages/logo/logo.vue"), (t.default = a.exports);
          },
        36:
          /*!*************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue?vue&type=template&id=140517e6&scoped=true& ***!
    \*************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, t, e) {
            e.r(t);
            var o = e(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./logo.vue?vue&type=template&id=140517e6&scoped=true& */ 37
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
        37:
          /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue?vue&type=template&id=140517e6&scoped=true& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, t, e) {
            e.r(t),
              e.d(t, "render", function () {
                return o;
              }),
              e.d(t, "staticRenderFns", function () {
                return i;
              }),
              e.d(t, "recyclableRender", function () {
                return r;
              }),
              e.d(t, "components", function () {});
            var o = function () {
                var n = this.$createElement,
                  t =
                    (this._self._c,
                    this.showTransition ? this.$t("welcome") : null),
                  e = this.showTransition ? this.$t("message") : null;
                this.$mp.data = Object.assign({}, { $root: { m0: t, m1: e } });
              },
              r = !1,
              i = [];
            o._withStripped = !0;
          },
        38:
          /*!*******************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue?vue&type=script&lang=js& ***!
    \*******************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./logo.vue?vue&type=script&lang=js& */ 39
              ),
              r = e.n(o);
            for (var i in o)
              ["default"].indexOf(i) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(i);
            t.default = r.a;
          },
        39:
          /*!**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue?vue&type=script&lang=js& ***!
    \**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            (function (n) {
              Object.defineProperty(t, "__esModule", { value: !0 }),
                (t.default = void 0);
              var o = e(/*! @/utils/i18n.js */ 40),
                r = {
                  computed: {
                    $t: function () {
                      return o.t;
                    },
                  },
                  data: function () {
                    return {
                      transitionCompleted: !1,
                      showTransition: !0,
                      color: "linear-gradient(to top, #fff, #636ef1)",
                    };
                  },
                  onLoad: function () {
                    var t = this;
                    setTimeout(function () {
                      1 != t.transitionCompleted &&
                        ((t.transitionCompleted = !0),
                        n.redirectTo({ url: "/pages/index/index" }));
                    }, 4e3);
                  },
                  methods: {
                    skipAnimation: function () {
                      (this.transitionCompleted = !0),
                        n.redirectTo({ url: "/pages/index/index" });
                    },
                  },
                };
              t.default = r;
            }).call(
              this,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        45:
          /*!***************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue?vue&type=style&index=0&id=140517e6&scoped=true&lang=css& ***!
    \***************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./logo.vue?vue&type=style&index=0&id=140517e6&scoped=true&lang=css& */ 46
              ),
              r = e.n(o);
            for (var i in o)
              ["default"].indexOf(i) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(i);
            t.default = r.a;
          },
        46:
          /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/logo/logo.vue?vue&type=style&index=0&id=140517e6&scoped=true&lang=css& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {},
      },
      [[34, "common/runtime", "common/vendor"]],
    ]);
  },
  { isPage: true, isComponent: true, currentFile: "pages/logo/logo.js" }
);
require("pages/logo/logo.js");
