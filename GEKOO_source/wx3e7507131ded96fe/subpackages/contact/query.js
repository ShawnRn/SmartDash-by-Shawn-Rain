(global.webpackJsonp = global.webpackJsonp || []).push([
  ["subpackages/contact/query"],
  {
    131:
      /*!***********************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Fcontact%2Fquery"} ***!
    \***********************************************************************************************/
      /*! no static exports found */
      function (t, e, n) {
        (function (t, e) {
          var a = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
          n(/*! uni-pages */ 26);
          a(n(/*! vue */ 25));
          var r = a(n(/*! ./subpackages/contact/query.vue */ 132));
          (t.__webpack_require_UNI_MP_PLUGIN__ = n), e(r.default);
        }).call(
          this,
          n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1).default,
          n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
            .createPage
        );
      },
    132:
      /*!****************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue ***!
    \****************************************************************************/
      /*! no static exports found */
      function (t, e, n) {
        n.r(e);
        var a = n(
            /*! ./query.vue?vue&type=template&id=47a724ce&scoped=true& */ 133
          ),
          r = n(/*! ./query.vue?vue&type=script&lang=js& */ 135);
        for (var c in r)
          ["default"].indexOf(c) < 0 &&
            (function (t) {
              n.d(e, t, function () {
                return r[t];
              });
            })(c);
        n(
          /*! ./query.vue?vue&type=style&index=0&id=47a724ce&scoped=true&lang=css& */ 137
        );
        var o = n(
            /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
          ),
          u = Object(o.default)(
            r.default,
            a.render,
            a.staticRenderFns,
            !1,
            null,
            "47a724ce",
            null,
            !1,
            a.components,
            void 0
          );
        (u.options.__file = "subpackages/contact/query.vue"),
          (e.default = u.exports);
      },
    133:
      /*!***********************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue?vue&type=template&id=47a724ce&scoped=true& ***!
    \***********************************************************************************************************************/
      /*! exports provided: render, staticRenderFns, recyclableRender, components */
      function (t, e, n) {
        n.r(e);
        var a = n(
          /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./query.vue?vue&type=template&id=47a724ce&scoped=true& */ 134
        );
        n.d(e, "render", function () {
          return a.render;
        }),
          n.d(e, "staticRenderFns", function () {
            return a.staticRenderFns;
          }),
          n.d(e, "recyclableRender", function () {
            return a.recyclableRender;
          }),
          n.d(e, "components", function () {
            return a.components;
          });
      },
    134:
      /*!***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue?vue&type=template&id=47a724ce&scoped=true& ***!
    \***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! exports provided: render, staticRenderFns, recyclableRender, components */
      function (t, e, n) {
        n.r(e),
          n.d(e, "render", function () {
            return a;
          }),
          n.d(e, "staticRenderFns", function () {
            return c;
          }),
          n.d(e, "recyclableRender", function () {
            return r;
          }),
          n.d(e, "components", function () {});
        var a = function () {
            var t = this.$createElement,
              e = (this._self._c, this.queryResult.length);
            this.$mp.data = Object.assign({}, { $root: { g0: e } });
          },
          r = !1,
          c = [];
        a._withStripped = !0;
      },
    135:
      /*!*****************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue?vue&type=script&lang=js& ***!
    \*****************************************************************************************************/
      /*! no static exports found */
      function (t, e, n) {
        n.r(e);
        var a = n(
            /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./query.vue?vue&type=script&lang=js& */ 136
          ),
          r = n.n(a);
        for (var c in a)
          ["default"].indexOf(c) < 0 &&
            (function (t) {
              n.d(e, t, function () {
                return a[t];
              });
            })(c);
        e.default = r.a;
      },
    136:
      /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue?vue&type=script&lang=js& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! no static exports found */
      function (t, e, n) {
        (function (t) {
          Object.defineProperty(e, "__esModule", { value: !0 }),
            (e.default = void 0);
          var n = {
            data: function () {
              return {
                trackingNumber: "",
                captchaInput: "",
                captchaText: "",
                captchaSum: 0,
                queryResult: [],
                inputError: !1,
                errorMessage: "",
              };
            },
            created: function () {
              this.generateCaptcha();
            },
            methods: {
              validateTrackingNumber: function () {
                return /^[a-zA-Z0-9]+$/.test(this.trackingNumber)
                  ? ((this.inputError = !1), !0)
                  : ((this.inputError = !0),
                    (this.errorMessage = "寄件单号只能包含字母和数字"),
                    !1);
              },
              generateCaptcha: function () {
                var t = Math.floor(10 * Math.random()),
                  e = Math.floor(10 * Math.random());
                (this.captchaSum = t + e),
                  (this.captchaText = "".concat(t, " + ").concat(e, " =?"));
              },
              queryStatus: function () {
                var e = this;
                this.validateTrackingNumber()
                  ? parseInt(this.captchaInput) === this.captchaSum
                    ? (this.generateCaptcha(),
                      t.request({
                        url: "https://www.gekoodriver.com/api/after_sale_query.php",
                        method: "GET",
                        data: { tracking_number: this.trackingNumber.trim() },
                        success: function (n) {
                          if ("success" === n.data.status) {
                            if ((console.log(n), "查询成功" != n.data.message))
                              return void t.showModal({
                                title: "提示",
                                content: n.data.message,
                                showCancel: !1,
                              });
                            var a = n.data.data.map(function (t) {
                              var e = t.返回单号
                                ? t.返回单号.replace(",", "\n")
                                : "";
                              return {
                                jddh: t.寄件单号,
                                fhdh: e,
                                sjrq: t.收件日期,
                                jjrq: t.寄件日期,
                                bz: t.备注,
                              };
                            });
                            e.queryResult = a;
                          } else
                            t.showModal({
                              title: "提示",
                              content: n.data.message,
                              showCancel: !1,
                            });
                        },
                        fail: function (e) {
                          console.error(e),
                            t.showToast({ title: "查询失败", icon: "none" });
                        },
                      }))
                    : t.showToast({ title: "验证码错误", icon: "none" })
                  : t.showToast({ title: this.errorMessage, icon: "none" });
              },
            },
          };
          e.default = n;
        }).call(
          this,
          n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
            .default
        );
      },
    137:
      /*!*************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue?vue&type=style&index=0&id=47a724ce&scoped=true&lang=css& ***!
    \*************************************************************************************************************************************/
      /*! no static exports found */
      function (t, e, n) {
        n.r(e);
        var a = n(
            /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./query.vue?vue&type=style&index=0&id=47a724ce&scoped=true&lang=css& */ 138
          ),
          r = n.n(a);
        for (var c in a)
          ["default"].indexOf(c) < 0 &&
            (function (t) {
              n.d(e, t, function () {
                return a[t];
              });
            })(c);
        e.default = r.a;
      },
    138:
      /*!*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/contact/query.vue?vue&type=style&index=0&id=47a724ce&scoped=true&lang=css& ***!
    \*****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! no static exports found */
      function (t, e, n) {},
  },
  [[131, "common/runtime", "common/vendor"]],
]);
