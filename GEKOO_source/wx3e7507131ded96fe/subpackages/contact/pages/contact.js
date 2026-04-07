(global.webpackJsonp = global.webpackJsonp || []).push([
  ["subpackages/contact/pages/contact"],
  {
    113:
      /*!*********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Fcontact%2Fpages%2Fcontact"} ***!
    \*********************************************************************************************************/
      /*! no static exports found */
      function (t, n, e) {
        (function (t, n) {
          var o = e(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
          e(/*! uni-pages */ 26);
          o(e(/*! vue */ 25));
          var c = o(e(/*! ./subpackages/contact/pages/contact.vue */ 114));
          (t.__webpack_require_UNI_MP_PLUGIN__ = e), n(c.default);
        }).call(
          this,
          e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1).default,
          e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
            .createPage
        );
      },
    114:
      /*!************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue ***!
    \************************************************************************************/
      /*! no static exports found */
      function (t, n, e) {
        e.r(n);
        var o = e(
            /*! ./contact.vue?vue&type=template&id=ae84ebb4&scoped=true& */ 115
          ),
          c = e(/*! ./contact.vue?vue&type=script&lang=js& */ 117);
        for (var a in c)
          ["default"].indexOf(a) < 0 &&
            (function (t) {
              e.d(n, t, function () {
                return c[t];
              });
            })(a);
        e(
          /*! ./contact.vue?vue&type=style&index=0&id=ae84ebb4&scoped=true&lang=css& */ 119
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
            "ae84ebb4",
            null,
            !1,
            o.components,
            void 0
          );
        (r.options.__file = "subpackages/contact/pages/contact.vue"),
          (n.default = r.exports);
      },
    115:
      /*!*******************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue?vue&type=template&id=ae84ebb4&scoped=true& ***!
    \*******************************************************************************************************************************/
      /*! exports provided: render, staticRenderFns, recyclableRender, components */
      function (t, n, e) {
        e.r(n);
        var o = e(
          /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./contact.vue?vue&type=template&id=ae84ebb4&scoped=true& */ 116
        );
        e.d(n, "render", function () {
          return o.render;
        }),
          e.d(n, "staticRenderFns", function () {
            return o.staticRenderFns;
          }),
          e.d(n, "recyclableRender", function () {
            return o.recyclableRender;
          }),
          e.d(n, "components", function () {
            return o.components;
          });
      },
    116:
      /*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue?vue&type=template&id=ae84ebb4&scoped=true& ***!
    \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! exports provided: render, staticRenderFns, recyclableRender, components */
      function (t, n, e) {
        e.r(n),
          e.d(n, "render", function () {
            return o;
          }),
          e.d(n, "staticRenderFns", function () {
            return a;
          }),
          e.d(n, "recyclableRender", function () {
            return c;
          }),
          e.d(n, "components", function () {});
        var o = function () {
            var t = this.$createElement,
              n = (this._self._c, this.$t("contact.companyName")),
              e = this.$t("contact.address"),
              o = this.$t("contact.phone"),
              c = this.$t("contact.button1"),
              a = this.$t("contact.button2"),
              i = this.$t("contact.button3");
            this.$mp.data = Object.assign(
              {},
              { $root: { m0: n, m1: e, m2: o, m3: c, m4: a, m5: i } }
            );
          },
          c = !1,
          a = [];
        o._withStripped = !0;
      },
    117:
      /*!*************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue?vue&type=script&lang=js& ***!
    \*************************************************************************************************************/
      /*! no static exports found */
      function (t, n, e) {
        e.r(n);
        var o = e(
            /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./contact.vue?vue&type=script&lang=js& */ 118
          ),
          c = e.n(o);
        for (var a in o)
          ["default"].indexOf(a) < 0 &&
            (function (t) {
              e.d(n, t, function () {
                return o[t];
              });
            })(a);
        n.default = c.a;
      },
    118:
      /*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue?vue&type=script&lang=js& ***!
    \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! no static exports found */
      function (t, n, e) {
        (function (t) {
          Object.defineProperty(n, "__esModule", { value: !0 }),
            (n.default = void 0);
          var o = e(/*! @/utils/i18n.js */ 40),
            c = {
              computed: {
                $t: function () {
                  return o.t;
                },
              },
              data: function () {
                return {
                  longitude: 120.41779277979279,
                  latitude: 31.292713854661812,
                  scale: 16,
                  markers: [
                    {
                      id: 0,
                      longitude: 120.41779277979279,
                      latitude: 31.292713854661812,
                      title: "智科",
                      showCoverView: !0,
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
                page_click_handle: function (n) {
                  switch (n) {
                    case 0:
                      t.showModal({
                        title: "寄件注意事项",
                        content:
                          "寄件时请将故障情况以及收件人地址和电话写到小纸条上一并寄过来，中间涉及到费用时会给您打电话确认。",
                        showCancel: !1,
                        confirmText: "确定",
                        confirmColor: "#3CC51F",
                        success: function (t) {
                          t.confirm
                            ? console.log("用户点击了确定")
                            : t.cancel && console.log("用户点击了取消");
                        },
                        fail: function (t) {
                          console.log(t);
                        },
                      });
                      break;
                    case 1:
                      t.showModal({
                        title: "工作时间",
                        content:
                          "业务咨询请拨打座机，售后相关请拨打手机号，工作时间：法定工作日8：00--16:30",
                        showCancel: !1,
                        confirmText: "确定",
                        confirmColor: "#3CC51F",
                        success: function (t) {
                          t.confirm
                            ? console.log("用户点击了确定")
                            : t.cancel && console.log("用户点击了取消");
                        },
                        fail: function (t) {
                          console.log(t);
                        },
                      });
                      break;
                    case 2:
                      t.navigateTo({ url: "/subpackages/contact/query" });
                  }
                },
              },
              onLoad: function () {
                t.setNavigationBarTitle({
                  title: this.$t("home_page.grid.item2"),
                });
              },
            };
          n.default = c;
        }).call(
          this,
          e(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
            .default
        );
      },
    119:
      /*!*********************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue?vue&type=style&index=0&id=ae84ebb4&scoped=true&lang=css& ***!
    \*********************************************************************************************************************************************/
      /*! no static exports found */
      function (t, n, e) {
        e.r(n);
        var o = e(
            /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./contact.vue?vue&type=style&index=0&id=ae84ebb4&scoped=true&lang=css& */ 120
          ),
          c = e.n(o);
        for (var a in o)
          ["default"].indexOf(a) < 0 &&
            (function (t) {
              e.d(n, t, function () {
                return o[t];
              });
            })(a);
        n.default = c.a;
      },
    120:
      /*!*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/pages/contact.vue?vue&type=style&index=0&id=ae84ebb4&scoped=true&lang=css& ***!
    \*************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! no static exports found */
      function (t, n, e) {},
  },
  [[113, "common/runtime", "common/vendor"]],
]);
