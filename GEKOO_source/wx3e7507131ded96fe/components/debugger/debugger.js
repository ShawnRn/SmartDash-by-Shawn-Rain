(global.webpackJsonp = global.webpackJsonp || []).push([
  ["components/debugger/debugger"],
  {
    312:
      /*!*******************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue ***!
    \*******************************************************************************/
      /*! no static exports found */
      function (e, t, l) {
        l.r(t);
        var n = l(
            /*! ./debugger.vue?vue&type=template&id=4d794f24&scoped=true& */ 313
          ),
          u = l(/*! ./debugger.vue?vue&type=script&lang=js& */ 315);
        for (var a in u)
          ["default"].indexOf(a) < 0 &&
            (function (e) {
              l.d(t, e, function () {
                return u[e];
              });
            })(a);
        l(
          /*! ./debugger.vue?vue&type=style&index=0&id=4d794f24&scoped=true&lang=css& */ 317
        );
        var i = l(
            /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
          ),
          o = Object(i.default)(
            u.default,
            n.render,
            n.staticRenderFns,
            !1,
            null,
            "4d794f24",
            null,
            !1,
            n.components,
            void 0
          );
        (o.options.__file = "components/debugger/debugger.vue"),
          (t.default = o.exports);
      },
    313:
      /*!**************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue?vue&type=template&id=4d794f24&scoped=true& ***!
    \**************************************************************************************************************************/
      /*! exports provided: render, staticRenderFns, recyclableRender, components */
      function (e, t, l) {
        l.r(t);
        var n = l(
          /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./debugger.vue?vue&type=template&id=4d794f24&scoped=true& */ 314
        );
        l.d(t, "render", function () {
          return n.render;
        }),
          l.d(t, "staticRenderFns", function () {
            return n.staticRenderFns;
          }),
          l.d(t, "recyclableRender", function () {
            return n.recyclableRender;
          }),
          l.d(t, "components", function () {
            return n.components;
          });
      },
    314:
      /*!**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue?vue&type=template&id=4d794f24&scoped=true& ***!
    \**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! exports provided: render, staticRenderFns, recyclableRender, components */
      function (e, t, l) {
        l.r(t),
          l.d(t, "render", function () {
            return n;
          }),
          l.d(t, "staticRenderFns", function () {
            return a;
          }),
          l.d(t, "recyclableRender", function () {
            return u;
          }),
          l.d(t, "components", function () {});
        var n = function () {
            var e = this.$createElement;
            this._self._c;
          },
          u = !1,
          a = [];
        n._withStripped = !0;
      },
    315:
      /*!********************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue?vue&type=script&lang=js& ***!
    \********************************************************************************************************/
      /*! no static exports found */
      function (e, t, l) {
        l.r(t);
        var n = l(
            /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./debugger.vue?vue&type=script&lang=js& */ 316
          ),
          u = l.n(n);
        for (var a in n)
          ["default"].indexOf(a) < 0 &&
            (function (e) {
              l.d(t, e, function () {
                return n[e];
              });
            })(a);
        t.default = u.a;
      },
    316:
      /*!***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue?vue&type=script&lang=js& ***!
    \***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! no static exports found */
      function (e, t, l) {
        (function (e) {
          Object.defineProperty(t, "__esModule", { value: !0 }),
            (t.default = void 0);
          var l = {
            props: {
              currentPolePairs: { type: [Number, String], default: "待检测" },
              currentSpeed: { type: [Number, String], default: "待检测" },
              idleThrottleVoltage: {
                type: [Number, String],
                default: "待检测",
              },
              fullThrottleVoltage: {
                type: [Number, String],
                default: "待检测",
              },
              busCurrent: { type: [Number, String], default: "待提供" },
              phaseCurrent: { type: [Number, String], default: "待提供" },
            },
            data: function () {
              return {
                inputFields: [
                  {
                    label: "电机峰值功率：",
                    type: "number",
                    placeholder: "请输入电机功率",
                    value: "",
                    unit: "W",
                  },
                  {
                    label: "电池单体标称电压：",
                    type: "digit",
                    placeholder: "请输入电池单体电压",
                    value: "",
                    unit: "V",
                  },
                  {
                    label: "电池单体放电截至电压：",
                    type: "digit",
                    placeholder: "请输入电池单体电压",
                    value: "",
                    unit: "V",
                  },
                  {
                    label: "电池单体充电截至电压：",
                    type: "digit",
                    placeholder: "请输入电池单体电压",
                    value: "",
                    unit: "V",
                  },
                  {
                    label: "电池串数：",
                    type: "digit",
                    placeholder: "请输入电池单体电压",
                    value: "",
                    unit: "串",
                  },
                  {
                    label: "持续放电电流：",
                    type: "number",
                    placeholder: "请输入持续放电电流",
                    value: "",
                    unit: "A",
                  },
                  {
                    label: "电池最大充电电流：",
                    type: "number",
                    placeholder: "请输入电池最大充电电流",
                    value: "",
                    unit: "A",
                  },
                  {
                    label: "轮胎直径：",
                    type: "number",
                    placeholder: "请输入轮胎直径",
                    value: "",
                    unit: "cm",
                  },
                ],
                resultFields: [
                  { label: "控制器母线", value: "待计算" },
                  { label: "控制器相线电流", value: "待计算" },
                  { label: "控制器弱磁电流", value: "待计算" },
                  { label: "电池欠压点", value: "待计算" },
                  { label: "电池过压点", value: "待计算" },
                  { label: "降载起始电压", value: "待计算" },
                  { label: "降载终止电压", value: "待计算" },
                ],
                polePairsFields: [
                  {
                    label: "当前的极对数",
                    value: this.currentPolePairs,
                    unit: "",
                  },
                  {
                    label: "当前的车速",
                    value: this.currentSpeed,
                    unit: "km/h",
                  },
                  { label: "计算出的电机极对数", value: "待计算", unit: "" },
                ],
                throttleFields: [
                  { label: "空转把电压：", value: this.idleThrottleVoltage },
                  { label: "满把电压：", value: this.fullThrottleVoltage },
                ],
              };
            },
            watch: {
              currentSpeed: function (e) {
                (this.currentSpeed = e),
                  (this.polePairsFields[1].value = this.currentSpeed);
              },
              idleThrottleVoltage: function (e) {
                (this.idleThrottleVoltage = e),
                  (this.throttleFields[0].value = e);
              },
              fullThrottleVoltage: function (e) {
                (this.fullThrottleVoltage = e),
                  (this.throttleFields[1].value = e);
              },
            },
            created: function () {
              this.loadInputValues();
            },
            methods: {
              loadInputValues: function () {
                var t = e.getStorageSync("inputValues");
                t &&
                  this.inputFields.forEach(function (e) {
                    t[e.label] && (e.value = t[e.label]);
                  });
              },
              saveInputValues: function () {
                var t = this.getInputValues();
                e.setStorageSync("inputValues", t);
              },
              handleInputChange: function (e, t) {
                "轮胎直径：" === e && this.$emit("tireRadiusChanged", t),
                  this.saveInputValues();
              },
              handleCalculateResults: function () {
                console.log("计算结果按钮被点击");
                var e = this.busCurrent,
                  t = (this.phaseCurrent, this.inputFields[0].value),
                  l = this.inputFields[1].value,
                  n = this.inputFields[2].value,
                  u = this.inputFields[3].value,
                  a = this.inputFields[4].value,
                  i = l * a,
                  o = (this.inputFields[3].value, this.inputFields[5].value),
                  r = t / i / 0.8,
                  s = 0,
                  d = 0,
                  c = 0;
                (s = e < o ? (r > e ? e : r) : r > o ? o : r),
                  (c =
                    (d =
                      e < 100
                        ? 3.25 * s + 0.5
                        : e < 200
                        ? 2.9 * s + 0.5
                        : e < 400
                        ? 2.2 * s + 0.5
                        : 2 * s) / 3),
                  (s = Math.round(s)),
                  (d = Math.round(d)),
                  (c = Math.round(c)),
                  console.log("最大电流：" + s),
                  console.log("相电流：" + d),
                  console.log("弱磁电流：" + c);
                var h,
                  p,
                  f,
                  g = 0;
                (g = u * a + 11) > 100 && (g = 100),
                  (p = (f = (h = n * a + 1) + 1) + 5),
                  (this.resultFields[0].value = s),
                  (this.resultFields[1].value = d),
                  (this.resultFields[2].value = c),
                  (this.resultFields[3].value = h),
                  (this.resultFields[4].value = g),
                  (this.resultFields[5].value = p),
                  (this.resultFields[6].value = f);
              },
              handleVolSet: function () {
                console.log("电压设置"), this.$emit("handleVolSet");
              },
              handleAutoSet: function () {
                console.log("自动设置按钮被点击"),
                  this.$emit("auto-set", this.resultFields);
              },
              handleAdaptPolePairs2: function () {
                this.$emit("adapt-Throttle", this.getInputValues());
              },
              handleAdaptPolePairs: function () {
                console.log("一键适配极对数按钮被点击"),
                  this.$emit("adapt-pole-pairs", this.getInputValues());
              },
              handleDetectThrottleVoltage: function (e) {
                console.log("检测空转把电压按钮被点击", e),
                  this.$emit("detect-throttle-voltage", e);
              },
              handleComfortTuning: function () {
                console.log("一键舒适调参按钮被点击"),
                  this.$emit("ebsCommondSet");
              },
              handlePerformanceTuning: function () {
                console.log("一键性能调参按钮被点击"),
                  this.$emit("ebsCloseSet");
              },
              handleImportParameters: function () {
                console.log("导入参数按钮被点击"),
                  this.$emit("import-parameters");
              },
              handleExportParameters: function () {
                console.log("导出参数按钮被点击"),
                  this.$emit("export-parameters");
              },
              getInputValues: function () {
                return this.inputFields.reduce(function (e, t) {
                  return (e[t.label] = t.value), e;
                }, {});
              },
            },
          };
          t.default = l;
        }).call(
          this,
          l(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
            .default
        );
      },
    317:
      /*!****************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue?vue&type=style&index=0&id=4d794f24&scoped=true&lang=css& ***!
    \****************************************************************************************************************************************/
      /*! no static exports found */
      function (e, t, l) {
        l.r(t);
        var n = l(
            /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./debugger.vue?vue&type=style&index=0&id=4d794f24&scoped=true&lang=css& */ 318
          ),
          u = l.n(n);
        for (var a in n)
          ["default"].indexOf(a) < 0 &&
            (function (e) {
              l.d(t, e, function () {
                return n[e];
              });
            })(a);
        t.default = u.a;
      },
    318:
      /*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/components/debugger/debugger.vue?vue&type=style&index=0&id=4d794f24&scoped=true&lang=css& ***!
    \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
      /*! no static exports found */
      function (e, t, l) {},
  },
]),
  (global.webpackJsonp = global.webpackJsonp || []).push([
    "components/debugger/debugger-create-component",
    {
      "components/debugger/debugger-create-component": function (e, t, l) {
        l("2").createComponent(l(312));
      },
    },
    [["components/debugger/debugger-create-component"]],
  ]);
