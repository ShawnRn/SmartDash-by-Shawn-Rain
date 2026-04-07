$gwx2_XC_1 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_1 || [];
    function gz$gwx2_XC_1_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "index"]);
        Z([3, "step"]);
        Z([[7], [3, "flowchartSteps"]]);
        Z(z[0]);
        Z([
          [2, "<"],
          [[7], [3, "index"]],
          [
            [2, "-"],
            [[6], [[7], [3, "$root"]], [3, "g0"]],
            [1, 1],
          ],
        ]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_1 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_1 = true;
    var x = ["./subpackages/info/flowchart/flowchart.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_1_1();
      var cF = _v();
      _(r, cF);
      var hG = function (cI, oH, oJ, gg) {
        var aL = _v();
        _(oJ, aL);
        if (_oz(z, 4, cI, oH, gg)) {
          aL.wxVkey = 1;
        }
        aL.wxXCkey = 1;
        return oJ;
      };
      cF.wxXCkey = 2;
      _2z(z, 2, hG, e, s, gg, cF, "step", "index", "index");
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_1";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_1();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/flowchart/flowchart.wxml"] = [
    $gwx2_XC_1,
    "./subpackages/info/flowchart/flowchart.wxml",
  ];
else
  __wxAppCode__["subpackages/info/flowchart/flowchart.wxml"] = $gwx2_XC_1(
    "./subpackages/info/flowchart/flowchart.wxml"
  );
__wxRoute = "subpackages/info/flowchart/flowchart";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/info/flowchart/flowchart.js";
define(
  "subpackages/info/flowchart/flowchart.js",
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
    require("../common/vendor.js"),
      (global.webpackJsonp = global.webpackJsonp || []).push([
        ["subpackages/info/flowchart/flowchart"],
        {
          188:
            /*!************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Finfo%2Fflowchart%2Fflowchart"} ***!
    \************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              (function (n, e) {
                var r = t(
                  /*! @babel/runtime/helpers/interopRequireDefault */ 4
                );
                t(/*! uni-pages */ 26);
                r(t(/*! vue */ 25));
                var o = r(
                  t(/*! ./subpackages/info/flowchart/flowchart.vue */ 189)
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
          189:
            /*!***************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue ***!
    \***************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              t.r(e);
              var r = t(
                  /*! ./flowchart.vue?vue&type=template&id=118080e0&scoped=true& */ 190
                ),
                o = t(/*! ./flowchart.vue?vue&type=script&lang=js& */ 192);
              for (var c in o)
                ["default"].indexOf(c) < 0 &&
                  (function (n) {
                    t.d(e, n, function () {
                      return o[n];
                    });
                  })(c);
              t(
                /*! ./flowchart.vue?vue&type=style&index=0&id=118080e0&scoped=true&lang=css& */ 194
              );
              var u = t(
                  /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
                ),
                a = Object(u.default)(
                  o.default,
                  r.render,
                  r.staticRenderFns,
                  !1,
                  null,
                  "118080e0",
                  null,
                  !1,
                  r.components,
                  void 0
                );
              (a.options.__file = "subpackages/info/flowchart/flowchart.vue"),
                (e.default = a.exports);
            },
          190:
            /*!**********************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue?vue&type=template&id=118080e0&scoped=true& ***!
    \**********************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (n, e, t) {
              t.r(e);
              var r = t(
                /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./flowchart.vue?vue&type=template&id=118080e0&scoped=true& */ 191
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
          191:
            /*!**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue?vue&type=template&id=118080e0&scoped=true& ***!
    \**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
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
                  return o;
                }),
                t.d(e, "components", function () {});
              var r = function () {
                  var n = this.$createElement,
                    e = (this._self._c, this.flowchartSteps.length);
                  this.$mp.data = Object.assign({}, { $root: { g0: e } });
                },
                o = !1,
                c = [];
              r._withStripped = !0;
            },
          192:
            /*!****************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue?vue&type=script&lang=js& ***!
    \****************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              t.r(e);
              var r = t(
                  /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./flowchart.vue?vue&type=script&lang=js& */ 193
                ),
                o = t.n(r);
              for (var c in r)
                ["default"].indexOf(c) < 0 &&
                  (function (n) {
                    t.d(e, n, function () {
                      return r[n];
                    });
                  })(c);
              e.default = o.a;
            },
          193:
            /*!***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue?vue&type=script&lang=js& ***!
    \***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              Object.defineProperty(e, "__esModule", { value: !0 }),
                (e.default = void 0);
              e.default = {
                data: function () {
                  return {
                    flowchartSteps: [
                      "步骤1：先了解保护板电机等参数",
                      "步骤2：保证自学习通过",
                      "步骤3：设置车型",
                      "步骤4：检查电机方向是否需要调整",
                      "步骤5：根据保护板设置电流",
                      "步骤6：适配转把",
                      "步骤7：三速和弱磁的设置，可借助调参助手",
                      "步骤8：起步和加速性能的调整",
                      "步骤9：根据需求设置功能开关",
                      "步骤10：结束",
                    ],
                  };
                },
              };
            },
          194:
            /*!************************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue?vue&type=style&index=0&id=118080e0&scoped=true&lang=css& ***!
    \************************************************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {
              t.r(e);
              var r = t(
                  /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./flowchart.vue?vue&type=style&index=0&id=118080e0&scoped=true&lang=css& */ 195
                ),
                o = t.n(r);
              for (var c in r)
                ["default"].indexOf(c) < 0 &&
                  (function (n) {
                    t.d(e, n, function () {
                      return r[n];
                    });
                  })(c);
              e.default = o.a;
            },
          195:
            /*!****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/flowchart/flowchart.vue?vue&type=style&index=0&id=118080e0&scoped=true&lang=css& ***!
    \****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (n, e, t) {},
        },
        [[188, "common/runtime", "common/vendor"]],
      ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/info/flowchart/flowchart.js",
  }
);
require("subpackages/info/flowchart/flowchart.js");
