$gwx2_XC_2 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_2 || [];
    function gz$gwx2_XC_2_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "container data-v-4c0e4f61"]);
        Z([[7], [3, "searchQuery"]]);
        Z([[7], [3, "selectedQuestion"]]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_2 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_2 = true;
    var x = ["./subpackages/info/pages/faq/faq.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_2_1();
      var eN = _n("view");
      _rz(z, eN, "class", 0, e, s, gg);
      var bO = _v();
      _(eN, bO);
      if (_oz(z, 1, e, s, gg)) {
        bO.wxVkey = 1;
      }
      var oP = _v();
      _(eN, oP);
      if (_oz(z, 2, e, s, gg)) {
        oP.wxVkey = 1;
      }
      bO.wxXCkey = 1;
      oP.wxXCkey = 1;
      _(r, eN);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_2";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_2();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/pages/faq/faq.wxml"] = [
    $gwx2_XC_2,
    "./subpackages/info/pages/faq/faq.wxml",
  ];
else
  __wxAppCode__["subpackages/info/pages/faq/faq.wxml"] = $gwx2_XC_2(
    "./subpackages/info/pages/faq/faq.wxml"
  );
__wxRoute = "subpackages/info/pages/faq/faq";
__wxRouteBegin = true;
__wxAppCurrentFile__ = "subpackages/info/pages/faq/faq.js";
define(
  "subpackages/info/pages/faq/faq.js",
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
    require("../../common/vendor.js"),
      (global.webpackJsonp = global.webpackJsonp || []).push([
        ["subpackages/info/pages/faq/faq"],
        {
          155:
            /*!********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Finfo%2Fpages%2Ffaq%2Ffaq"} ***!
    \********************************************************************************************************/
            /*! no static exports found */
            function (e, n, t) {
              (function (e, n) {
                var a = t(
                  /*! @babel/runtime/helpers/interopRequireDefault */ 4
                );
                t(/*! uni-pages */ 26);
                a(t(/*! vue */ 25));
                var r = a(t(/*! ./subpackages/info/pages/faq/faq.vue */ 156));
                (e.__webpack_require_UNI_MP_PLUGIN__ = t), n(r.default);
              }).call(
                this,
                t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
                  .default,
                t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                  .createPage
              );
            },
          156:
            /*!*********************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue ***!
    \*********************************************************************************/
            /*! no static exports found */
            function (e, n, t) {
              t.r(n);
              var a = t(
                  /*! ./faq.vue?vue&type=template&id=4c0e4f61&scoped=true& */ 157
                ),
                r = t(/*! ./faq.vue?vue&type=script&lang=js& */ 159);
              for (var i in r)
                ["default"].indexOf(i) < 0 &&
                  (function (e) {
                    t.d(n, e, function () {
                      return r[e];
                    });
                  })(i);
              t(
                /*! ./faq.vue?vue&type=style&index=0&id=4c0e4f61&scoped=true&lang=css& */ 162
              );
              var o = t(
                  /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
                ),
                u = Object(o.default)(
                  r.default,
                  a.render,
                  a.staticRenderFns,
                  !1,
                  null,
                  "4c0e4f61",
                  null,
                  !1,
                  a.components,
                  void 0
                );
              (u.options.__file = "subpackages/info/pages/faq/faq.vue"),
                (n.default = u.exports);
            },
          157:
            /*!****************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue?vue&type=template&id=4c0e4f61&scoped=true& ***!
    \****************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (e, n, t) {
              t.r(n);
              var a = t(
                /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./faq.vue?vue&type=template&id=4c0e4f61&scoped=true& */ 158
              );
              t.d(n, "render", function () {
                return a.render;
              }),
                t.d(n, "staticRenderFns", function () {
                  return a.staticRenderFns;
                }),
                t.d(n, "recyclableRender", function () {
                  return a.recyclableRender;
                }),
                t.d(n, "components", function () {
                  return a.components;
                });
            },
          158:
            /*!****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue?vue&type=template&id=4c0e4f61&scoped=true& ***!
    \****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! exports provided: render, staticRenderFns, recyclableRender, components */
            function (e, n, t) {
              t.r(n),
                t.d(n, "render", function () {
                  return a;
                }),
                t.d(n, "staticRenderFns", function () {
                  return i;
                }),
                t.d(n, "recyclableRender", function () {
                  return r;
                }),
                t.d(n, "components", function () {});
              var a = function () {
                  var e = this.$createElement,
                    n = (this._self._c, this.$t("faq_page.area0")),
                    t = this.$t("faq_page.area1"),
                    a = this.$t("faq_page.area2"),
                    r = this.$t("faq_page.area3"),
                    i = this.$t("faq_page.area4"),
                    o = this.$t("faq_page.area5");
                  this.$mp.data = Object.assign(
                    {},
                    { $root: { m0: n, m1: t, m2: a, m3: r, m4: i, m5: o } }
                  );
                },
                r = !1,
                i = [];
              a._withStripped = !0;
            },
          159:
            /*!**********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue?vue&type=script&lang=js& ***!
    \**********************************************************************************************************/
            /*! no static exports found */
            function (e, n, t) {
              t.r(n);
              var a = t(
                  /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./faq.vue?vue&type=script&lang=js& */ 160
                ),
                r = t.n(a);
              for (var i in a)
                ["default"].indexOf(i) < 0 &&
                  (function (e) {
                    t.d(n, e, function () {
                      return a[e];
                    });
                  })(i);
              n.default = r.a;
            },
          160:
            /*!*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue?vue&type=script&lang=js& ***!
    \*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (e, n, t) {
              (function (e) {
                var a = t(
                  /*! @babel/runtime/helpers/interopRequireDefault */ 4
                );
                Object.defineProperty(n, "__esModule", { value: !0 }),
                  (n.default = void 0);
                var r = t(/*! @/utils/i18n.js */ 40),
                  i = a(t(/*! @/utils/faqdata.js */ 161)),
                  o = {
                    computed: {
                      $t: function () {
                        return r.t;
                      },
                    },
                    data: function () {
                      return {
                        searchQuery: "",
                        selectedQuestion: null,
                        filteredQuestions: [],
                      };
                    },
                    methods: {
                      faq_click_handle: function (n) {
                        e.navigateTo({
                          url: "/subpackages/info/pages/faqlist/faqlist?categoryIndex=".concat(
                            n
                          ),
                        });
                      },
                      handleSearchChange: function () {
                        var e = this;
                        this.filteredQuestions = i.default.flatMap(function (
                          n
                        ) {
                          return n.questions.filter(function (n) {
                            return n.question
                              .toLowerCase()
                              .includes(e.searchQuery.toLowerCase());
                          });
                        });
                      },
                      handleQuestionClick: function (e) {
                        this.selectedQuestion = e;
                      },
                      handleCloseModal: function () {
                        this.selectedQuestion = null;
                      },
                    },
                    onLoad: function () {
                      this.handleSearchChange(),
                        e.setNavigationBarTitle({
                          title: this.$t("home_page.grid.item3"),
                        });
                    },
                  };
                n.default = o;
              }).call(
                this,
                t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
                  .default
              );
            },
          162:
            /*!******************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue?vue&type=style&index=0&id=4c0e4f61&scoped=true&lang=css& ***!
    \******************************************************************************************************************************************/
            /*! no static exports found */
            function (e, n, t) {
              t.r(n);
              var a = t(
                  /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./faq.vue?vue&type=style&index=0&id=4c0e4f61&scoped=true&lang=css& */ 163
                ),
                r = t.n(a);
              for (var i in a)
                ["default"].indexOf(i) < 0 &&
                  (function (e) {
                    t.d(n, e, function () {
                      return a[e];
                    });
                  })(i);
              n.default = r.a;
            },
          163:
            /*!**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/faq/faq.vue?vue&type=style&index=0&id=4c0e4f61&scoped=true&lang=css& ***!
    \**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
            /*! no static exports found */
            function (e, n, t) {},
        },
        [
          [
            155,
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
    currentFile: "subpackages/info/pages/faq/faq.js",
  }
);
require("subpackages/info/pages/faq/faq.js");
