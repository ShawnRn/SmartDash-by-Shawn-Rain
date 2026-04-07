$gwx1_XC_1 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx1_XC_1 || [];
    function gz$gwx1_XC_1_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx1_XC_1_1)
        return __WXML_GLOBAL__.ops_cached.$gwx1_XC_1_1;
      __WXML_GLOBAL__.ops_cached.$gwx1_XC_1_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
      })(__WXML_GLOBAL__.ops_cached.$gwx1_XC_1_1);
      return __WXML_GLOBAL__.ops_cached.$gwx1_XC_1_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx1_XC_1 = z;
    __WXML_GLOBAL__.ops_init.$gwx1_XC_1 = true;
    var x = ["./subpackages/product/pages/product.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx1_XC_1_1();
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx1_XC_1";
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
if (__vd_version_info__.delayedGwx || false) $gwx1_XC_1();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/product/pages/product.wxml"] = [
    $gwx1_XC_1,
    "./subpackages/product/pages/product.wxml",
  ];
else
  __wxAppCode__["subpackages/product/pages/product.wxml"] = $gwx1_XC_1(
    "./subpackages/product/pages/product.wxml"
  );
__wxRoute = "subpackages/product/pages/product";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/product/pages/product.js";
define(
  "subpackages/product/pages/product.js",
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
      ["subpackages/product/pages/product"],
      {
        139:
          /*!*********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Fproduct%2Fpages%2Fproduct"} ***!
    \*********************************************************************************************************/
          /*! no static exports found */
          function (e, t, n) {
            (function (e, t) {
              var i = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
              n(/*! uni-pages */ 26);
              i(n(/*! vue */ 25));
              var a = i(n(/*! ./subpackages/product/pages/product.vue */ 140));
              (e.__webpack_require_UNI_MP_PLUGIN__ = n), t(a.default);
            }).call(
              this,
              n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                .default,
              n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .createPage
            );
          },
        140:
          /*!************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue ***!
    \************************************************************************************/
          /*! no static exports found */
          function (e, t, n) {
            n.r(t);
            var i = n(
                /*! ./product.vue?vue&type=template&id=54b232f8&scoped=true& */ 141
              ),
              a = n(/*! ./product.vue?vue&type=script&lang=js& */ 143);
            for (var o in a)
              ["default"].indexOf(o) < 0 &&
                (function (e) {
                  n.d(t, e, function () {
                    return a[e];
                  });
                })(o);
            n(
              /*! ./product.vue?vue&type=style&index=0&id=54b232f8&scoped=true&lang=css& */ 145
            );
            var r = n(
                /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
              ),
              g = Object(r.default)(
                a.default,
                i.render,
                i.staticRenderFns,
                !1,
                null,
                "54b232f8",
                null,
                !1,
                i.components,
                void 0
              );
            (g.options.__file = "subpackages/product/pages/product.vue"),
              (t.default = g.exports);
          },
        141:
          /*!*******************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue?vue&type=template&id=54b232f8&scoped=true& ***!
    \*******************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (e, t, n) {
            n.r(t);
            var i = n(
              /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./product.vue?vue&type=template&id=54b232f8&scoped=true& */ 142
            );
            n.d(t, "render", function () {
              return i.render;
            }),
              n.d(t, "staticRenderFns", function () {
                return i.staticRenderFns;
              }),
              n.d(t, "recyclableRender", function () {
                return i.recyclableRender;
              }),
              n.d(t, "components", function () {
                return i.components;
              });
          },
        142:
          /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue?vue&type=template&id=54b232f8&scoped=true& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! exports provided: render, staticRenderFns, recyclableRender, components */
          function (e, t, n) {
            n.r(t),
              n.d(t, "render", function () {
                return i;
              }),
              n.d(t, "staticRenderFns", function () {
                return o;
              }),
              n.d(t, "recyclableRender", function () {
                return a;
              }),
              n.d(t, "components", function () {});
            var i = function () {
                var e = this.$createElement;
                this._self._c;
              },
              a = !1,
              o = [];
            i._withStripped = !0;
          },
        143:
          /*!*************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue?vue&type=script&lang=js& ***!
    \*************************************************************************************************************/
          /*! no static exports found */
          function (e, t, n) {
            n.r(t);
            var i = n(
                /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./product.vue?vue&type=script&lang=js& */ 144
              ),
              a = n.n(i);
            for (var o in i)
              ["default"].indexOf(o) < 0 &&
                (function (e) {
                  n.d(t, e, function () {
                    return i[e];
                  });
                })(o);
            t.default = a.a;
          },
        144:
          /*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue?vue&type=script&lang=js& ***!
    \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (e, t, n) {
            (function (e) {
              Object.defineProperty(t, "__esModule", { value: !0 }),
                (t.default = void 0);
              var i = n(/*! @/utils/i18n.js */ 40),
                a = "https://www.gekoodriver.com/static/product/",
                o = "https://www.gekoodriver.com/static/product/xqy/",
                r = {
                  computed: {
                    $t: function () {
                      return i.t;
                    },
                  },
                  data: function () {
                    return {
                      products: [
                        {
                          model: "7235",
                          image: a + "7235.jpg",
                          textLine2: "母线35 相线120",
                          textLine3: "长127 宽110 高48.5",
                          realShotImage: a + "7235.jpg",
                          sizeImage: o + "7235xqy.jpg",
                        },
                        {
                          model: "GK7260S",
                          image: a + "7260s.jpg",
                          textLine2: "母线60 相线220",
                          textLine3: "长179 宽97 高52",
                          realShotImage: a + "7260s.jpg",
                          sizeImage: "",
                        },
                        {
                          model: "72260",
                          image: a + "72260.jpg",
                          textLine2: "母线80 相线260",
                          textLine3: "长170 宽100 高55",
                          realShotImage: a + "72260.jpg",
                          sizeImage: o + "72260xqy.jpg",
                        },
                        {
                          model: "CP72260",
                          image: a + "cp72260new.jpg",
                          textLine2: "母线80 相线260",
                          textLine3: "长179 宽97 高52",
                          realShotImage: a + "cp72260new.jpg",
                          sizeImage: o + "72260xqy.jpg",
                        },
                        {
                          model: "72350",
                          image: a + "72350.jpg",
                          textLine2: "母线120 相线350",
                          textLine3: "长195 宽113 高60",
                          realShotImage: a + "72350.jpg",
                          sizeImage: o + "72350xqy.jpg",
                        },
                        {
                          model: "72450",
                          image: a + "72450.jpg",
                          textLine2: "母线200 相线450",
                          textLine3: "长195 宽113 高60",
                          realShotImage: a + "72450.jpg",
                          sizeImage: "",
                        },
                        {
                          model: "72500",
                          image: a + "72500.jpg",
                          textLine2: "母线200 相线500",
                          textLine3: "长167 宽102 高64",
                          realShotImage: a + "72500.jpg",
                          sizeImage: o + "72500xqy.jpg",
                        },
                        {
                          model: "72550",
                          image: a + "72550.jpg",
                          textLine2: "母线250 相线550",
                          textLine3: "长205 宽137 高65",
                          realShotImage: a + "72550.jpg",
                          sizeImage: "",
                        },
                        {
                          model: "72550P",
                          image: a + "72550P.jpg",
                          textLine2: "母线350 相线750",
                          textLine3: "长205 宽137 高65",
                          realShotImage: a + "72550P.jpg",
                          sizeImage: "",
                        },
                        {
                          model: "72850",
                          image: a + "72850.jpg",
                          textLine2: "母线400 相线850",
                          textLine3: "长210 宽135 高75",
                          realShotImage: a + "72850.jpg",
                          sizeImage: o + "72850xqy.jpg",
                        },
                        {
                          model: "721200",
                          image: a + "721200.jpg",
                          textLine2: "母线600 相线1200",
                          textLine3: "长220 宽148 高68",
                          realShotImage: a + "721200.jpg",
                          sizeImage: o + "721200xqy.jpg",
                        },
                      ],
                    };
                  },
                  methods: {
                    showProductRealShot: function (t) {
                      var n = this.products[t];
                      e.previewImage({ urls: [n.realShotImage] });
                    },
                    showProductSize: function (t) {
                      var n = this.products[t];
                      "" != n.sizeImage
                        ? e.previewImage({ urls: [n.sizeImage] })
                        : e.showToast({
                            title: "等待补充",
                            icon: "none",
                            duration: 2e3,
                          });
                    },
                    showSizetRealShot: function (t) {
                      0 == t
                        ? e.navigateTo({
                            url: "/pages/webview/webview?url=".concat(
                              encodeURIComponent(
                                "https://mp.weixin.qq.com/s/9pwKRH2zBL8PonZYOrhpjA?token=1294366477&lang=zh_CN"
                              )
                            ),
                          })
                        : e.previewImage({ urls: [a + "gbjk.jpg"] });
                    },
                  },
                  onLoad: function () {
                    e.setNavigationBarTitle({
                      title: this.$t("home_page.grid.item4"),
                    });
                  },
                };
              t.default = r;
            }).call(
              this,
              n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                .default
            );
          },
        145:
          /*!*********************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue?vue&type=style&index=0&id=54b232f8&scoped=true&lang=css& ***!
    \*********************************************************************************************************************************************/
          /*! no static exports found */
          function (e, t, n) {
            n.r(t);
            var i = n(
                /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./product.vue?vue&type=style&index=0&id=54b232f8&scoped=true&lang=css& */ 146
              ),
              a = n.n(i);
            for (var o in i)
              ["default"].indexOf(o) < 0 &&
                (function (e) {
                  n.d(t, e, function () {
                    return i[e];
                  });
                })(o);
            t.default = a.a;
          },
        146:
          /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/product/pages/product.vue?vue&type=style&index=0&id=54b232f8&scoped=true&lang=css& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
          /*! no static exports found */
          function (e, t, n) {},
      },
      [[139, "common/runtime", "common/vendor"]],
    ]);
  },
  {
    isPage: true,
    isComponent: true,
    currentFile: "subpackages/product/pages/product.js",
  }
);
require("subpackages/product/pages/product.js");
