$gwx_XC_7 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_7 || [];
    function gz$gwx_XC_7_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_7_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_7_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_7_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_7_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_7_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_7 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_7 = true;
    var x = ["./subpackages/apppolicy/userAgreement/userAgreement.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_7_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_7";
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
if (__vd_version_info__.delayedGwx || false) $gwx_XC_7();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/apppolicy/userAgreement/userAgreement.wxml"] = [
    $gwx_XC_7,
    "./subpackages/apppolicy/userAgreement/userAgreement.wxml",
  ];
else
  __wxAppCode__["subpackages/apppolicy/userAgreement/userAgreement.wxml"] =
    $gwx_XC_7("./subpackages/apppolicy/userAgreement/userAgreement.wxml");
__wxRoute = "subpackages/apppolicy/userAgreement/userAgreement";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/apppolicy/userAgreement/userAgreement.js";
define(
  "subpackages/apppolicy/userAgreement/userAgreement.js",
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
      ["subpackages/apppolicy/userAgreement/userAgreement"],
      {
        100:
          /*!***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue?vue&type=template&id=46b94ff3&scoped=true& ***!
    \***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, e, t) {
            t.r(e),
              t.d(e, "render", function () {
                return r;
              }),
              t.d(e, "staticRenderFns", function () {
                return c;
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
              c = [];
            r._withStripped = !0;
          },
        101:
          /*!*****************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue?vue&type=script&lang=js& ***!
    \*****************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var r = t(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./userAgreement.vue?vue&type=script&lang=js& */ 102
              ),
              u = t.n(r);
            for (var c in r)
              ["default"].indexOf(c) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return r[n];
                  });
                })(c);
            e.default = u.a;
          },
        102:
          /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue?vue&type=script&lang=js& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            Object.defineProperty(e, "__esModule", { value: !0 }),
              (e.default = void 0);
            e.default = {
              data: function () {
                return {};
              },
            };
          },
        103:
          /*!*************************************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue?vue&type=style&index=0&id=46b94ff3&scoped=true&lang=css& ***!
    \*************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var r = t(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./userAgreement.vue?vue&type=style&index=0&id=46b94ff3&scoped=true&lang=css& */ 104
              ),
              u = t.n(r);
            for (var c in r)
              ["default"].indexOf(c) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return r[n];
                  });
                })(c);
            e.default = u.a;
          },
        104:
          /*!*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue?vue&type=style&index=0&id=46b94ff3&scoped=true&lang=css& ***!
    \*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {},
        97:
          /*!*************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Fapppolicy%2FuserAgreement%2FuserAgreement"} ***!
    \*************************************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            (function (n, e) {
              var r = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              t(/*! uni-pages */ 26);
              r(t(/*! vue */ 25));
              var u = r(
                t(
                  /*! ./subpackages/apppolicy/userAgreement/userAgreement.vue */ 98
                )
              );
              (n.__webpack_require_UNI_MP_PLUGIN__ = t), e(u.default);
            }).call(
              this,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        98:
          /*!****************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue ***!
    \****************************************************************************************************/
          /*! no static exports found */
          function (n, e, t) {
            t.r(e);
            var r = t(
                /*! ./userAgreement.vue?vue&type=template&id=46b94ff3&scoped=true& */ 99
              ),
              u = t(/*! ./userAgreement.vue?vue&type=script&lang=js& */ 101);
            for (var c in u)
              ["default"].indexOf(c) < 0 &&
                (function (n) {
                  t.d(e, n, function () {
                    return u[n];
                  });
                })(c);
            t(
              /*! ./userAgreement.vue?vue&type=style&index=0&id=46b94ff3&scoped=true&lang=css& */ 103
            );
            var o = t(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              a = Object(o.default)(
                u.default,
                r.render,
                r.staticRenderFns,
                !1,
                null,
                "46b94ff3",
                null,
                !1,
                r.components,
                void 0
              );
            (a.options.__file =
              "subpackages/apppolicy/userAgreement/userAgreement.vue"),
              (e.default = a.exports);
          },
        99:
          /*!***********************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/apppolicy/userAgreement/userAgreement.vue?vue&type=template&id=46b94ff3&scoped=true& ***!
    \***********************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (n, e, t) {
            t.r(e);
            var r = t(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./userAgreement.vue?vue&type=template&id=46b94ff3&scoped=true& */ 100
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
      },
      [[97, "common/runtime", "common/vendor"]],
    ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/apppolicy/userAgreement/userAgreement.js",
  }
);
require("subpackages/apppolicy/userAgreement/userAgreement.js");
