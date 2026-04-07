$gwx_XC_1 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_1 || [];
    function gz$gwx_XC_1_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_1_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_1_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_1_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_1_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_1_1;
    }
    function gz$gwx_XC_1_2() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_1_2)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_1_2;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_1_2 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "__l"]);
        Z([3, "data-v-3e202046"]);
        Z([3, "/static/lx2.png"]);
        Z([3, "f4372d00-1"]);
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_1_2);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_1_2;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_1 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_1 = true;
    var x = ["./components/cy_snow/cy_snow.wxml", "./pages/test/test.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_1_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    d_[x[1]] = {};
    var m1 = function (e, s, r, gg) {
      var z = gz$gwx_XC_1_2();
      var o0B = _mz(
        z,
        "cysnow",
        ["bind:__l", 0, "class", 1, "img", 1, "vueId", 2],
        [],
        e,
        s,
        gg
      );
      _(r, o0B);
      return r;
    };
    e_[x[1]] = { f: m1, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_1";
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
if (__vd_version_info__.delayedGwx || false) $gwx_XC_1();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["components/cy_snow/cy_snow.wxml"] = [
    $gwx_XC_1,
    "./components/cy_snow/cy_snow.wxml",
  ];
else
  __wxAppCode__["components/cy_snow/cy_snow.wxml"] = $gwx_XC_1(
    "./components/cy_snow/cy_snow.wxml"
  );
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["pages/test/test.wxml"] = [$gwx_XC_1, "./pages/test/test.wxml"];
else
  __wxAppCode__["pages/test/test.wxml"] = $gwx_XC_1("./pages/test/test.wxml");
__wxRoute = "components/cy_snow/cy_snow";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "components/cy_snow/cy_snow.js";
define(
  "components/cy_snow/cy_snow.js",
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
      ["components/cy_snow/cy_snow"],
      {
        326:
          /*!*****************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue ***!
    \*****************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! ./cy_snow.vue?vue&type=template&id=6970575e&scoped=true& */ 327
              ),
              c = e(/*! ./cy_snow.vue?vue&type=script&lang=js& */ 329);
            for (var r in c)
              ["default"].indexOf(r) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return c[n];
                  });
                })(r);
            e(
              /*! ./cy_snow.vue?vue&type=style&index=0&id=6970575e&lang=scss&scoped=true& */ 331
            );
            var i = e(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              u = Object(i.default)(
                c.default,
                o.render,
                o.staticRenderFns,
                !1,
                null,
                "6970575e",
                null,
                !1,
                o.components,
                void 0
              );
            (u.options.__file = "components/cy_snow/cy_snow.vue"),
              (t.default = u.exports);
          },
        327:
          /*!************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue?vue&type=template&id=6970575e&scoped=true& ***!
    \************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, t, e) {
            e.r(t);
            var o = e(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./cy_snow.vue?vue&type=template&id=6970575e&scoped=true& */ 328
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
        328:
          /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue?vue&type=template&id=6970575e&scoped=true& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, t, e) {
            e.r(t),
              e.d(t, "render", function () {
                return o;
              }),
              e.d(t, "staticRenderFns", function () {
                return r;
              }),
              e.d(t, "recyclableRender", function () {
                return c;
              }),
              e.d(t, "components", function () {});
            var o = function () {
                var n = this.$createElement;
                this._self._c;
              },
              c = !1,
              r = [];
            o._withStripped = !0;
          },
        329:
          /*!******************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue?vue&type=script&lang=js& ***!
    \******************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./cy_snow.vue?vue&type=script&lang=js& */ 330
              ),
              c = e.n(o);
            for (var r in o)
              ["default"].indexOf(r) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(r);
            t.default = c.a;
          },
        330:
          /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue?vue&type=script&lang=js& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            (function (n) {
              Object.defineProperty(t, "__esModule", { value: !0 }),
                (t.default = void 0);
              var e = {
                props: {
                  img: { type: String, default: "" },
                  bjimg: { type: String, default: "" },
                  nums: { type: Number, default: 100 },
                  size: { type: Number, default: 2 },
                },
                data: function () {
                  return { conwidth: "100vw", conheight: "93vh", snows: [] };
                },
                updated: function () {},
                mounted: function () {
                  this.getsnow();
                },
                methods: {
                  getsnow: function () {
                    var t = n.getSystemInfoSync(),
                      e = t.windowHeight,
                      o = t.windowWidth;
                    (this.conwidth = o + "px"), (this.conheight = e + "px");
                    for (
                      var c = function (n) {
                          return Math.random() * n;
                        },
                        r = this.nums,
                        i = 0;
                      i < r;
                      i++
                    ) {
                      var u = c(this.size);
                      this.snows.push({
                        top: c(e) + "px",
                        left: c(o) + "px",
                        animationDelay: c(2) + "s",
                        wh: (u < 5 ? 5 : u) + "px",
                      });
                    }
                    console.log(this.snows);
                  },
                },
              };
              t.default = e;
            }).call(
              this,
              e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        331:
          /*!***************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue?vue&type=style&index=0&id=6970575e&lang=scss&scoped=true& ***!
    \***************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {
            e.r(t);
            var o = e(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--8-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--8-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-2!./node_modules/postcss-loader/src??ref--8-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/sass-loader/dist/cjs.js??ref--8-oneOf-1-4!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-5!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./cy_snow.vue?vue&type=style&index=0&id=6970575e&lang=scss&scoped=true& */ 332
              ),
              c = e.n(o);
            for (var r in o)
              ["default"].indexOf(r) < 0 &&
                (function (n) {
                  e.d(t, n, function () {
                    return o[n];
                  });
                })(r);
            t.default = c.a;
          },
        332:
          /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--8-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--8-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-2!./node_modules/postcss-loader/src??ref--8-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/sass-loader/dist/cjs.js??ref--8-oneOf-1-4!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-5!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/cy_snow/cy_snow.vue?vue&type=style&index=0&id=6970575e&lang=scss&scoped=true& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, t, e) {},
      },
    ]),
      (global.webpackJsonp = global.webpackJsonp || []).push([
        "components/cy_snow/cy_snow-create-component",
        {
          "components/cy_snow/cy_snow-create-component": function (n, t, e) {
            e("2").createComponent(e(326));
          },
        },
        [["components/cy_snow/cy_snow-create-component"]],
      ]);
  },
  {
    isPage: false,
    isComponent: true,
    currentFile: "components/cy_snow/cy_snow.js",
  }
);
require("components/cy_snow/cy_snow.js");
__wxRoute = "pages/test/test";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "pages/test/test.js";
define(
  "pages/test/test.js",
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
      ["pages/test/test"],
      {
        73:
          /*!*************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"pages%2Ftest%2Ftest"} ***!
    \*************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            (function (e, n) {
              var o = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              t(/*! uni-pages */ 26);
              o(t(/*! vue */ 25));
              var r = o(t(/*! ./pages/test/test.vue */ 74));
              (e.__webpack_require_UNI_MP_PLUGIN__ = t), n(r.default);
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        74:
          /*!******************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue ***!
    \******************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            t.r(n);
            var o = t(
                /*! ./test.vue?vue&type=template&id=3e202046&scoped=true& */ 75
              ),
              r = t(/*! ./test.vue?vue&type=script&lang=js& */ 77);
            for (var c in r)
              ["default"].indexOf(c) < 0 &&
                (function (e) {
                  t.d(n, e, function () {
                    return r[e];
                  });
                })(c);
            t(
              /*! ./test.vue?vue&type=style&index=0&id=3e202046&scoped=true&lang=css& */ 79
            );
            var i = t(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              a = Object(i.default)(
                r.default,
                o.render,
                o.staticRenderFns,
                !1,
                null,
                "3e202046",
                null,
                !1,
                o.components,
                void 0
              );
            (a.options.__file = "pages/test/test.vue"), (n.default = a.exports);
          },
        75:
          /*!*************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue?vue&type=template&id=3e202046&scoped=true& ***!
    \*************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (e, n, t) {
            t.r(n);
            var o = t(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./test.vue?vue&type=template&id=3e202046&scoped=true& */ 76
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
        76:
          /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue?vue&type=template&id=3e202046&scoped=true& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
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
                return r;
              }),
              t.d(n, "components", function () {});
            var o = function () {
                var e = this.$createElement;
                this._self._c;
              },
              r = !1,
              c = [];
            o._withStripped = !0;
          },
        77:
          /*!*******************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue?vue&type=script&lang=js& ***!
    \*******************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            t.r(n);
            var o = t(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./test.vue?vue&type=script&lang=js& */ 78
              ),
              r = t.n(o);
            for (var c in o)
              ["default"].indexOf(c) < 0 &&
                (function (e) {
                  t.d(n, e, function () {
                    return o[e];
                  });
                })(c);
            n.default = r.a;
          },
        78:
          /*!**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue?vue&type=script&lang=js& ***!
    \**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            (function (e) {
              var o = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              Object.defineProperty(n, "__esModule", { value: !0 }),
                (n.default = void 0);
              var r = o(t(/*! @babel/runtime/regenerator */ 53)),
                c = o(t(/*! @babel/runtime/helpers/asyncToGenerator */ 55)),
                i = e.getFileSystemManager(),
                a = {
                  name: "ImageSelector",
                  components: {
                    Cysnow: function () {
                      t.e(
                        /*! require.ensure | components/cy_snow/cy_snow */ "components/cy_snow/cy_snow"
                      )
                        .then(
                          function () {
                            return resolve(
                              t(/*! @/components/cy_snow/cy_snow.vue */ 326)
                            );
                          }.bind(null, t)
                        )
                        .catch(t.oe);
                    },
                  },
                  data: function () {
                    return {
                      imageSrcList: [null, null, null],
                      IMAGE_KEY: "selectedImages",
                    };
                  },
                  onLoad: function () {
                    var n = e.getStorageSync(this.IMAGE_KEY);
                    n && n.length && (this.imageSrcList = n);
                  },
                  methods: {
                    chooseImages: function () {
                      var n,
                        t = this;
                      e.chooseImage({
                        count: 3,
                        success:
                          ((n = (0, c.default)(
                            r.default.mark(function n(o) {
                              var c, a, l, u, s;
                              return r.default.wrap(function (n) {
                                for (;;)
                                  switch ((n.prev = n.next)) {
                                    case 0:
                                      for (
                                        c = o.tempFilePaths, a = 0;
                                        a < c.length;
                                        a++
                                      ) {
                                        (l = c[a]),
                                          (u = "image".concat(a, ".jpg")),
                                          (s = ""
                                            .concat(e.env.USER_DATA_PATH, "/")
                                            .concat(u)),
                                          (t.imageSrcList[a] = s);
                                        try {
                                          i.copyFileSync(l, s);
                                        } catch (e) {
                                          console.error(e);
                                        }
                                        console.log("saveOK");
                                      }
                                      t.$forceUpdate(),
                                        e.setStorageSync(
                                          t.IMAGE_KEY,
                                          t.imageSrcList
                                        ),
                                        console.log("save finisih"),
                                        e.showToast({
                                          title: "设置完成，重启刷新",
                                          icon: "info",
                                          duration: 2e3,
                                        });
                                    case 6:
                                    case "end":
                                      return n.stop();
                                  }
                              }, n);
                            })
                          )),
                          function (e) {
                            return n.apply(this, arguments);
                          }),
                        fail: function (e) {
                          console.error("选择图片失败:", e);
                        },
                      });
                    },
                    deleteAllImages_APP: function () {
                      e.getSavedFileList({
                        success: function (n) {
                          if (n.fileList.length > 0)
                            for (var t = 0; t < n.fileList.length; t++)
                              e.removeSavedFile({
                                filePath: n.fileList[t].filePath,
                                complete: function (e) {
                                  console.log(e);
                                },
                              });
                        },
                      });
                    },
                    deleteAllImages: function () {
                      (this.imageSrcList = [null, null, null]),
                        this.$forceUpdate();
                      try {
                        e.showToast({
                          title: "已恢复默认轮播图",
                          icon: "info",
                          duration: 2e3,
                        }),
                          e.removeStorageSync(this.IMAGE_KEY);
                      } catch (e) {
                        console.error("清除本地存储图片路径出错:", e);
                      }
                    },
                  },
                };
              n.default = a;
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        79:
          /*!***************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue?vue&type=style&index=0&id=3e202046&scoped=true&lang=css& ***!
    \***************************************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {
            t.r(n);
            var o = t(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./test.vue?vue&type=style&index=0&id=3e202046&scoped=true&lang=css& */ 80
              ),
              r = t.n(o);
            for (var c in o)
              ["default"].indexOf(c) < 0 &&
                (function (e) {
                  t.d(n, e, function () {
                    return o[e];
                  });
                })(c);
            n.default = r.a;
          },
        80:
          /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/test/test.vue?vue&type=style&index=0&id=3e202046&scoped=true&lang=css& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (e, n, t) {},
      },
      [[73, "common/runtime", "common/vendor"]],
    ]);
  },
  { isPage: true, isComponent: true, currentFile: "pages/test/test.js" }
);
require("pages/test/test.js");
