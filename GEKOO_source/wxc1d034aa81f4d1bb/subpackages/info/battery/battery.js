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
            var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
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
            var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
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
