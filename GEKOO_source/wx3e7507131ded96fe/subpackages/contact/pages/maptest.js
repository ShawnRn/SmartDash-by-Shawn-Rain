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
          e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1).default,
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
