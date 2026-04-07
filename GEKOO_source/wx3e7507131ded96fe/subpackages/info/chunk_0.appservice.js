$gwx2_XC_0 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_0 || [];
    function gz$gwx2_XC_0_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "battery-settings-page data-v-7f55b84c"]);
        Z([
          [2, "==="],
          [[7], [3, "currentTab"]],
          [1, 0],
        ]);
        Z([
          [2, "==="],
          [[7], [3, "currentTab"]],
          [1, 1],
        ]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_0 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_0 = true;
    var x = ["./subpackages/info/battery/battery.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_0_1();
      var oB = _n("view");
      _rz(z, oB, "class", 0, e, s, gg);
      var xC = _v();
      _(oB, xC);
      if (_oz(z, 1, e, s, gg)) {
        xC.wxVkey = 1;
      }
      var oD = _v();
      _(oB, oD);
      if (_oz(z, 2, e, s, gg)) {
        oD.wxVkey = 1;
      }
      xC.wxXCkey = 1;
      oD.wxXCkey = 1;
      _(r, oB);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_0";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_0();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/battery/battery.wxml"] = [
    $gwx2_XC_0,
    "./subpackages/info/battery/battery.wxml",
  ];
else
  __wxAppCode__["subpackages/info/battery/battery.wxml"] = $gwx2_XC_0(
    "./subpackages/info/battery/battery.wxml"
  );
__wxRoute = "subpackages/info/battery/battery";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/info/battery/battery.js";
define(
  "subpackages/info/battery/battery.js",
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
        ["subpackages/info/battery/battery"],
        {
          196:
            /*!********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Finfo%2Fbattery%2Fbattery"} ***!
    \********************************************************************************************************/
            /*! no static exports found */
            function (t, e, n) {
              (function (t, e) {
                var r = n(
                  /*! @babel/runtime/helpers/interopRequireDefault */ 4
                );
                n(/*! uni-pages */ 26);
                r(n(/*! vue */ 25));
                var a = r(n(/*! ./subpackages/info/battery/battery.vue */ 197));
                (t.__webpack_require_UNI_MP_PLUGIN__ = n), e(a.default);
              }).call(
                this,
                n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                  .default,
                n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                  .createPage
              );
            },
          197:
            /*!***********************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue ***!
    \***********************************************************************************/
            /*! no static exports found */
            function (t, e, n) {
              n.r(e);
              var r = n(
                  /*! ./battery.vue?vue&type=template&id=7f55b84c&scoped=true& */ 198
                ),
                a = n(/*! ./battery.vue?vue&type=script&lang=js& */ 200);
              for (var o in a)
                ["default"].indexOf(o) < 0 &&
                  (function (t) {
                    n.d(e, t, function () {
                      return a[t];
                    });
                  })(o);
              n(
                /*! ./battery.vue?vue&type=style&index=0&id=7f55b84c&scoped=true&lang=css& */ 204
              );
              var c = n(
                  /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
                ),
                i = Object(c.default)(
                  a.default,
                  r.render,
                  r.staticRenderFns,
                  !1,
                  null,
                  "7f55b84c",
                  null,
                  !1,
                  r.components,
                  void 0
                );
              (i.options.__file = "subpackages/info/battery/battery.vue"),
                (e.default = i.exports);
            },
          198:
            /*!******************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue?vue&type=template&id=7f55b84c&scoped=true& ***!
    \******************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (t, e, n) {
              n.r(e);
              var r = n(
                /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./battery.vue?vue&type=template&id=7f55b84c&scoped=true& */ 199
              );
              n.d(e, "render", function () {
                return r.render;
              }),
                n.d(e, "staticRenderFns", function () {
                  return r.staticRenderFns;
                }),
                n.d(e, "recyclableRender", function () {
                  return r.recyclableRender;
                }),
                n.d(e, "components", function () {
                  return r.components;
                });
            },
          199:
            /*!******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue?vue&type=template&id=7f55b84c&scoped=true& ***!
    \******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (t, e, n) {
              n.r(e),
                n.d(e, "render", function () {
                  return r;
                }),
                n.d(e, "staticRenderFns", function () {
                  return o;
                }),
                n.d(e, "recyclableRender", function () {
                  return a;
                }),
                n.d(e, "components", function () {});
              var r = function () {
                  var t = this.$createElement;
                  this._self._c;
                },
                a = !1,
                o = [];
              r._withStripped = !0;
            },
          200:
            /*!************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue?vue&type=script&lang=js& ***!
    \************************************************************************************************************/
            /*! no static exports found */
            function (t, e, n) {
              n.r(e);
              var r = n(
                  /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./battery.vue?vue&type=script&lang=js& */ 201
                ),
                a = n.n(r);
              for (var o in r)
                ["default"].indexOf(o) < 0 &&
                  (function (t) {
                    n.d(e, t, function () {
                      return r[t];
                    });
                  })(o);
              e.default = a.a;
            },
          201:
            /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue?vue&type=script&lang=js& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (t, e, n) {
              (function (t) {
                var r = n(
                  /*! @babel/runtime/helpers/interopRequireDefault */ 4
                );
                Object.defineProperty(e, "__esModule", { value: !0 }),
                  (e.default = void 0);
                var a = r(
                    n(/*! @babel/runtime/helpers/objectWithoutProperties */ 202)
                  ),
                  o = r(n(/*! @babel/runtime/helpers/defineProperty */ 11)),
                  c = ["remark"];
                function i(t, e) {
                  var n = Object.keys(t);
                  if (Object.getOwnPropertySymbols) {
                    var r = Object.getOwnPropertySymbols(t);
                    e &&
                      (r = r.filter(function (e) {
                        return Object.getOwnPropertyDescriptor(t, e).enumerable;
                      })),
                      n.push.apply(n, r);
                  }
                  return n;
                }
                function u(t) {
                  for (var e = 1; e < arguments.length; e++) {
                    var n = null != arguments[e] ? arguments[e] : {};
                    e % 2
                      ? i(Object(n), !0).forEach(function (e) {
                          (0, o.default)(t, e, n[e]);
                        })
                      : Object.getOwnPropertyDescriptors
                      ? Object.defineProperties(
                          t,
                          Object.getOwnPropertyDescriptors(n)
                        )
                      : i(Object(n)).forEach(function (e) {
                          Object.defineProperty(
                            t,
                            e,
                            Object.getOwnPropertyDescriptor(n, e)
                          );
                        });
                  }
                  return t;
                }
                var s = {
                  data: function () {
                    return {
                      currentTab: 0,
                      battery1: {},
                      battery2: {},
                      defaultParams: {
                        remark: "电池参数设置",
                        busCurrent: 40,
                        phaseCurrent: 220,
                        weakMagnetCurrent: 40,
                        overVoltagePoint: 95,
                        underVoltagePoint: 44,
                        loadReductionStartVoltage: 52,
                        loadReductionEndVoltage: 45,
                      },
                    };
                  },
                  onLoad: function () {
                    this.loadStoredData();
                  },
                  methods: {
                    switchTab: function (t) {
                      this.currentTab = t;
                    },
                    loadStoredData: function () {
                      var e = t.getStorageSync("battery1Params");
                      this.battery1 = e || u({}, this.defaultParams);
                      var n = t.getStorageSync("battery2Params");
                      this.battery2 = n || u({}, this.defaultParams);
                    },
                    resetToDefault: function (e) {
                      0 === e
                        ? (this.battery1 = u({}, this.defaultParams))
                        : (this.battery2 = u({}, this.defaultParams)),
                        t.showToast({
                          title: "已恢复默认值",
                          icon: "none",
                          duration: 2e3,
                        });
                    },
                    confirmSettings: function (e) {
                      var n, r;
                      0 === e
                        ? ((n = this.battery1), (r = "battery1Params"))
                        : ((n = this.battery2), (r = "battery2Params")),
                        t.setStorageSync(r, n);
                      var o = n,
                        i = (o.remark, (0, a.default)(o, c));
                      t.$emit("batteryParamsUpdated", {
                        batteryIndex: e,
                        params: i,
                      }),
                        t.showToast({
                          title: "设置已保存",
                          icon: "success",
                          duration: 1500,
                        }),
                        setTimeout(function () {
                          t.navigateBack({ delta: 1 });
                        }, 1e3);
                    },
                    cancel: function () {
                      t.navigateBack({ delta: 1 });
                    },
                  },
                };
                e.default = s;
              }).call(
                this,
                n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                  .default
              );
            },
          204:
            /*!********************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue?vue&type=style&index=0&id=7f55b84c&scoped=true&lang=css& ***!
    \********************************************************************************************************************************************/
            /*! no static exports found */
            function (t, e, n) {
              n.r(e);
              var r = n(
                  /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./battery.vue?vue&type=style&index=0&id=7f55b84c&scoped=true&lang=css& */ 205
                ),
                a = n.n(r);
              for (var o in r)
                ["default"].indexOf(o) < 0 &&
                  (function (t) {
                    n.d(e, t, function () {
                      return r[t];
                    });
                  })(o);
              e.default = a.a;
            },
          205:
            /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/battery/battery.vue?vue&type=style&index=0&id=7f55b84c&scoped=true&lang=css& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (t, e, n) {},
        },
        [
          [
            196,
            "common/runtime",
            "common/vendor",
            "subpackages/info/common/vendor",
          ],
        ],
      ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/info/battery/battery.js",
  }
);
require("subpackages/info/battery/battery.js");
