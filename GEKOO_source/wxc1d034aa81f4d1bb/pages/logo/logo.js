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
          e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1).default,
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
