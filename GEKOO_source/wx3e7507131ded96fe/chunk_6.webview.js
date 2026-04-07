$gwx_XC_6 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_6 || [];
    function gz$gwx_XC_6_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_6_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_6_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_6_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "privacy-policy-container _div data-v-53dc1269"]);
        Z([3, "policy-title _h1 data-v-53dc1269"]);
        Z([3, "苏州智骐驱动科技有限公司隐私政策"]);
        Z([3, "policy-content _div data-v-53dc1269"]);
        Z([3, "section-title _h2 data-v-53dc1269"]);
        Z([3, "一、引言"]);
        Z([3, "section-text _p data-v-53dc1269"]);
        Z([
          3,
          "苏州智骐驱动科技有限公司（以下简称 “我们”）高度重视用户隐私保护。本隐私政策旨在清晰阐述我们在运营 “GEKOO”（以下简称 “本 App”）过程中，如何收集、使用、存储、共享及保护您的个人信息。当您使用本 App 时，即表示您已充分理解并同意本隐私政策所述内容。",
        ]);
        Z(z[4]);
        Z([3, "二、我们收集的个人信息及权限使用说明"]);
        Z(z[6]);
        Z([3, "（一）蓝牙权限"]);
        Z(z[6]);
        Z([
          3,
          "为实现本 App 通过 BLE 蓝牙与本公司生产的电机控制器进行数据交互、通讯，对控制器内部参数进行调试读写以及固件升级等核心功能，我们需要获取您的蓝牙权限。只有在开启蓝牙权限后，App 才能与电机控制器建立连接，完成参数读写和调试工作。我们不会通过蓝牙权限收集您的个人身份信息，仅用于设备间的数据通讯。",
        ]);
        Z(z[6]);
        Z([3, "（二）定位权限"]);
        Z(z[6]);
        Z([
          3,
          "由于在移动设备中，开启 BLE 蓝牙功能需要同时打开定位服务，因此我们在使用蓝牙权限时，会同步获取定位权限。但请您放心，我们获取定位权限并非用于追踪您的地理位置，也不会收集您的位置信息用于其他目的，仅为满足蓝牙功能的正常运行需求。此外，在 “联系我们” 页面，为了准确展示我们公司的位置，我们集成了第三方高德地图 SDK，该 SDK 仅用于地图定位展示我们的位置 。",
        ]);
        Z(z[6]);
        Z([3, "（三）相册访问权限"]);
        Z(z[6]);
        Z([
          3,
          "为了提升您的使用体验，满足您个性化设置的需求，在本 App 的部分功能场景中（如主页的轮播图设置），您可以从相册中选择喜欢的图片进行显示。我们仅在您主动选择使用该功能时，获取相册访问权限，读取您选择的图片信息，且不会对相册内其他图片进行任何操作，所读取的图片仅用于您设置的个性化展示。",
        ]);
        Z(z[6]);
        Z([3, "（四）本地读写访问权限"]);
        Z(z[6]);
        Z([
          3,
          "为方便您对重要数据进行管理，我们为您提供数据备份功能。当您使用该功能时，我们将获取本地读写访问权限，将您指定的部分数据保存到本地设备。这些数据仅用于您个人的数据备份，我们不会收集或获取您本地设备中的其他数据。",
        ]);
        Z(z[6]);
        Z([3, "（五）获取网络状态信息"]);
        Z(z[6]);
        Z([
          3,
          "我们的应用可能会收集设备的网络连接状态（如Wi-Fi或移动数据），以便：确保应用功能正常运行（例如在无网络时提供离线模式），优化内容加载速度和质量，这些信息仅在设备本地使用，不会被用于识别个人身份，也不会与第三方共享。",
        ]);
        Z(z[4]);
        Z([3, "三、个人信息的使用目的"]);
        Z(z[6]);
        Z([
          3,
          "1. 实现本 App 与电机控制器的正常数据交互和通讯，保障调试、读写参数及固件升级等核心功能的顺利运行。",
        ]);
        Z(z[6]);
        Z([3, "2. 满足您个性化设置需求，提升使用体验。"]);
        Z(z[6]);
        Z([3, "3. 为您提供便捷的数据备份功能，方便您管理个人数据。"]);
        Z(z[6]);
        Z([
          3,
          "4. 在 “联系我们” 页面通过高德地图 SDK 展示公司位置，便于您了解我们的地理位置。",
        ]);
        Z(z[4]);
        Z([3, "四、个人信息的共享与披露"]);
        Z(z[6]);
        Z([3, "我们不会将您的个人信息共享给任何第三方，除非以下情况："]);
        Z(z[6]);
        Z([
          3,
          "1. 根据法律法规的要求，需要向相关政府部门、司法机关等披露信息。",
        ]);
        Z(z[6]);
        Z([
          3,
          "2. 在发生合并、收购、资产转让等交易时，可能会将您的个人信息转移给相关的交易方，但我们会要求交易方继续遵守本隐私政策，保护您的个人信息安全。",
        ]);
        Z(z[6]);
        Z([
          3,
          "3. 我们集成的第三方高德地图 SDK，在您访问 “联系我们” 页面时，该 SDK 会按照其自身隐私政策收集和使用您的位置信息 。高德地图 SDK 的具体隐私政策可通过 [具体链接] 查看，您也可以在手机设置中管理相关权限。",
        ]);
        Z(z[4]);
        Z([3, "五、个人信息的存储与安全"]);
        Z(z[6]);
        Z([
          3,
          "我们将在中华人民共和国境内存储您的个人信息，存储期限根据业务需求和法律法规要求确定。我们采取了多种安全技术和管理措施来保护您的个人信息，包括但不限于数据加密、访问控制、定期安全审计等，尽力防止信息泄露、损毁或丢失。对于第三方高德地图 SDK，我们会督促其采取合理的安全保护措施。但请注意，互联网环境并非绝对安全，我们会持续努力提升安全防护水平，降低风险。",
        ]);
        Z(z[4]);
        Z([3, "六、您的权利"]);
        Z(z[6]);
        Z([3, "1. 查询权：您有权查询我们收集的关于您的个人信息。"]);
        Z(z[6]);
        Z([
          3,
          "2. 更正权：如果您发现我们收集的个人信息存在错误，您可以通过本 App 的相关功能或联系我们进行更正。",
        ]);
        Z(z[6]);
        Z([
          3,
          "3. 删除权：在符合法律法规规定的条件下，您有权要求我们删除您的个人信息。对于通过高德地图 SDK 收集的位置信息，您也可以根据其隐私政策指引进行删除操作。",
        ]);
        Z(z[6]);
        Z([
          3,
          "4. 撤回同意权：您可以随时在设备的系统设置中关闭相应权限，撤回对我们收集个人信息的同意。但请您注意，撤回同意可能会导致部分功能无法正常使用。若您关闭与高德地图 SDK 相关的位置权限，“联系我们” 页面的地图定位展示功能可能无法正常使用。",
        ]);
        Z(z[4]);
        Z([3, "七、隐私政策的更新"]);
        Z(z[6]);
        Z([
          3,
          "我们可能会根据业务发展、法律法规变化或其他原因更新本隐私政策。更新后的隐私政策将在本 App 内显著位置发布，或通过其他合理方式通知您。对于因第三方 SDK 相关条款变化导致的隐私政策更新，我们也会及时告知。请您定期查看本隐私政策，以了解我们对您个人信息处理方式的最新情况。若您在本隐私政策更新后继续使用本 App，即视为您已同意更新后的隐私政策。",
        ]);
        Z(z[4]);
        Z([3, "八、联系我们"]);
        Z(z[6]);
        Z([
          3,
          "如果您对本隐私政策有任何疑问、意见或建议，或者需要行使您的个人信息权利，请通过以下方式联系我们：",
        ]);
        Z(z[6]);
        Z([3, "1. 公司名称：苏州智骐驱动科技有限公司"]);
        Z(z[6]);
        Z([
          3,
          "2. 联系地址：江苏省苏州市吴中区光福镇龙山南路 9 号 1 号楼 5 楼",
        ]);
        Z(z[6]);
        Z([3, "3. 联系电话：0512-66919004"]);
        Z(z[6]);
        Z([3, "4. 电子邮箱： sxq@gekoodriver.com"]);
        Z(z[6]);
        Z([3, "我们将在收到您的反馈后，尽快与您取得联系并处理相关问题。"]);
        Z([3, "update-info _p data-v-53dc1269"]);
        Z([3, "更新日期：2025-05-05"]);
        Z(z[82]);
        Z([3, "生效日期：2025-05-05"]);
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_6_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_6_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_6 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_6 = true;
    var x = ["./subpackages/apppolicy/privacyPolicy/privacyPolicy.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_6_1();
      var o0O = _n("view");
      _rz(z, o0O, "class", 0, e, s, gg);
      var cAP = _n("view");
      _rz(z, cAP, "class", 1, e, s, gg);
      var oBP = _oz(z, 2, e, s, gg);
      _(cAP, oBP);
      _(o0O, cAP);
      var lCP = _n("view");
      _rz(z, lCP, "class", 3, e, s, gg);
      var aDP = _n("view");
      _rz(z, aDP, "class", 4, e, s, gg);
      var tEP = _oz(z, 5, e, s, gg);
      _(aDP, tEP);
      _(lCP, aDP);
      var eFP = _n("view");
      _rz(z, eFP, "class", 6, e, s, gg);
      var bGP = _oz(z, 7, e, s, gg);
      _(eFP, bGP);
      _(lCP, eFP);
      var oHP = _n("view");
      _rz(z, oHP, "class", 8, e, s, gg);
      var xIP = _oz(z, 9, e, s, gg);
      _(oHP, xIP);
      _(lCP, oHP);
      var oJP = _n("view");
      _rz(z, oJP, "class", 10, e, s, gg);
      var fKP = _oz(z, 11, e, s, gg);
      _(oJP, fKP);
      _(lCP, oJP);
      var cLP = _n("view");
      _rz(z, cLP, "class", 12, e, s, gg);
      var hMP = _oz(z, 13, e, s, gg);
      _(cLP, hMP);
      _(lCP, cLP);
      var oNP = _n("view");
      _rz(z, oNP, "class", 14, e, s, gg);
      var cOP = _oz(z, 15, e, s, gg);
      _(oNP, cOP);
      _(lCP, oNP);
      var oPP = _n("view");
      _rz(z, oPP, "class", 16, e, s, gg);
      var lQP = _oz(z, 17, e, s, gg);
      _(oPP, lQP);
      _(lCP, oPP);
      var aRP = _n("view");
      _rz(z, aRP, "class", 18, e, s, gg);
      var tSP = _oz(z, 19, e, s, gg);
      _(aRP, tSP);
      _(lCP, aRP);
      var eTP = _n("view");
      _rz(z, eTP, "class", 20, e, s, gg);
      var bUP = _oz(z, 21, e, s, gg);
      _(eTP, bUP);
      _(lCP, eTP);
      var oVP = _n("view");
      _rz(z, oVP, "class", 22, e, s, gg);
      var xWP = _oz(z, 23, e, s, gg);
      _(oVP, xWP);
      _(lCP, oVP);
      var oXP = _n("view");
      _rz(z, oXP, "class", 24, e, s, gg);
      var fYP = _oz(z, 25, e, s, gg);
      _(oXP, fYP);
      _(lCP, oXP);
      var cZP = _n("view");
      _rz(z, cZP, "class", 26, e, s, gg);
      var h1P = _oz(z, 27, e, s, gg);
      _(cZP, h1P);
      _(lCP, cZP);
      var o2P = _n("view");
      _rz(z, o2P, "class", 28, e, s, gg);
      var c3P = _oz(z, 29, e, s, gg);
      _(o2P, c3P);
      _(lCP, o2P);
      var o4P = _n("view");
      _rz(z, o4P, "class", 30, e, s, gg);
      var l5P = _oz(z, 31, e, s, gg);
      _(o4P, l5P);
      _(lCP, o4P);
      var a6P = _n("view");
      _rz(z, a6P, "class", 32, e, s, gg);
      var t7P = _oz(z, 33, e, s, gg);
      _(a6P, t7P);
      _(lCP, a6P);
      var e8P = _n("view");
      _rz(z, e8P, "class", 34, e, s, gg);
      var b9P = _oz(z, 35, e, s, gg);
      _(e8P, b9P);
      _(lCP, e8P);
      var o0P = _n("view");
      _rz(z, o0P, "class", 36, e, s, gg);
      var xAQ = _oz(z, 37, e, s, gg);
      _(o0P, xAQ);
      _(lCP, o0P);
      var oBQ = _n("view");
      _rz(z, oBQ, "class", 38, e, s, gg);
      var fCQ = _oz(z, 39, e, s, gg);
      _(oBQ, fCQ);
      _(lCP, oBQ);
      var cDQ = _n("view");
      _rz(z, cDQ, "class", 40, e, s, gg);
      var hEQ = _oz(z, 41, e, s, gg);
      _(cDQ, hEQ);
      _(lCP, cDQ);
      var oFQ = _n("view");
      _rz(z, oFQ, "class", 42, e, s, gg);
      var cGQ = _oz(z, 43, e, s, gg);
      _(oFQ, cGQ);
      _(lCP, oFQ);
      var oHQ = _n("view");
      _rz(z, oHQ, "class", 44, e, s, gg);
      var lIQ = _oz(z, 45, e, s, gg);
      _(oHQ, lIQ);
      _(lCP, oHQ);
      var aJQ = _n("view");
      _rz(z, aJQ, "class", 46, e, s, gg);
      var tKQ = _oz(z, 47, e, s, gg);
      _(aJQ, tKQ);
      _(lCP, aJQ);
      var eLQ = _n("view");
      _rz(z, eLQ, "class", 48, e, s, gg);
      var bMQ = _oz(z, 49, e, s, gg);
      _(eLQ, bMQ);
      _(lCP, eLQ);
      var oNQ = _n("view");
      _rz(z, oNQ, "class", 50, e, s, gg);
      var xOQ = _oz(z, 51, e, s, gg);
      _(oNQ, xOQ);
      _(lCP, oNQ);
      var oPQ = _n("view");
      _rz(z, oPQ, "class", 52, e, s, gg);
      var fQQ = _oz(z, 53, e, s, gg);
      _(oPQ, fQQ);
      _(lCP, oPQ);
      var cRQ = _n("view");
      _rz(z, cRQ, "class", 54, e, s, gg);
      var hSQ = _oz(z, 55, e, s, gg);
      _(cRQ, hSQ);
      _(lCP, cRQ);
      var oTQ = _n("view");
      _rz(z, oTQ, "class", 56, e, s, gg);
      var cUQ = _oz(z, 57, e, s, gg);
      _(oTQ, cUQ);
      _(lCP, oTQ);
      var oVQ = _n("view");
      _rz(z, oVQ, "class", 58, e, s, gg);
      var lWQ = _oz(z, 59, e, s, gg);
      _(oVQ, lWQ);
      _(lCP, oVQ);
      var aXQ = _n("view");
      _rz(z, aXQ, "class", 60, e, s, gg);
      var tYQ = _oz(z, 61, e, s, gg);
      _(aXQ, tYQ);
      _(lCP, aXQ);
      var eZQ = _n("view");
      _rz(z, eZQ, "class", 62, e, s, gg);
      var b1Q = _oz(z, 63, e, s, gg);
      _(eZQ, b1Q);
      _(lCP, eZQ);
      var o2Q = _n("view");
      _rz(z, o2Q, "class", 64, e, s, gg);
      var x3Q = _oz(z, 65, e, s, gg);
      _(o2Q, x3Q);
      _(lCP, o2Q);
      var o4Q = _n("view");
      _rz(z, o4Q, "class", 66, e, s, gg);
      var f5Q = _oz(z, 67, e, s, gg);
      _(o4Q, f5Q);
      _(lCP, o4Q);
      var c6Q = _n("view");
      _rz(z, c6Q, "class", 68, e, s, gg);
      var h7Q = _oz(z, 69, e, s, gg);
      _(c6Q, h7Q);
      _(lCP, c6Q);
      var o8Q = _n("view");
      _rz(z, o8Q, "class", 70, e, s, gg);
      var c9Q = _oz(z, 71, e, s, gg);
      _(o8Q, c9Q);
      _(lCP, o8Q);
      var o0Q = _n("view");
      _rz(z, o0Q, "class", 72, e, s, gg);
      var lAR = _oz(z, 73, e, s, gg);
      _(o0Q, lAR);
      _(lCP, o0Q);
      var aBR = _n("view");
      _rz(z, aBR, "class", 74, e, s, gg);
      var tCR = _oz(z, 75, e, s, gg);
      _(aBR, tCR);
      _(lCP, aBR);
      var eDR = _n("view");
      _rz(z, eDR, "class", 76, e, s, gg);
      var bER = _oz(z, 77, e, s, gg);
      _(eDR, bER);
      _(lCP, eDR);
      var oFR = _n("view");
      _rz(z, oFR, "class", 78, e, s, gg);
      var xGR = _oz(z, 79, e, s, gg);
      _(oFR, xGR);
      _(lCP, oFR);
      var oHR = _n("view");
      _rz(z, oHR, "class", 80, e, s, gg);
      var fIR = _oz(z, 81, e, s, gg);
      _(oHR, fIR);
      _(lCP, oHR);
      _(o0O, lCP);
      var cJR = _n("view");
      _rz(z, cJR, "class", 82, e, s, gg);
      var hKR = _oz(z, 83, e, s, gg);
      _(cJR, hKR);
      _(o0O, cJR);
      var oLR = _n("view");
      _rz(z, oLR, "class", 84, e, s, gg);
      var cMR = _oz(z, 85, e, s, gg);
      _(oLR, cMR);
      _(o0O, oLR);
      _(r, o0O);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_6";
        var main = e_[path].f;
        if (typeof global === "undefined") global = {};
        global.f = $gdc(f_[path], "", 1);
        if (
          typeof outerGlobal.__webview_engine_version__ != "undefined" &&
          outerGlobal.__webview_engine_version__ + 1e-6 >= 0.02 + 1e-6 &&
          outerGlobal.__mergeData__
        ) {
          env = outerGlobal.__mergeData__(env, dd);
        }
        try {
          main(env, {}, root, global);
          _tsd(root);
          if (
            typeof outerGlobal.__webview_engine_version__ == "undefined" ||
            outerGlobal.__webview_engine_version__ + 1e-6 < 0.01 + 1e-6
          ) {
            return _ev(root);
          }
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
if (__vd_version_info__.delayedGwx || false) $gwx_XC_6();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/apppolicy/privacyPolicy/privacyPolicy.wxml"] = [
    $gwx_XC_6,
    "./subpackages/apppolicy/privacyPolicy/privacyPolicy.wxml",
  ];
else
  __wxAppCode__["subpackages/apppolicy/privacyPolicy/privacyPolicy.wxml"] =
    $gwx_XC_6("./subpackages/apppolicy/privacyPolicy/privacyPolicy.wxml");

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/apppolicy/privacyPolicy/privacyPolicy.wxss"] =
    setCssToHead(
      [
        ".",
        [1],
        "privacy-policy-container.",
        [1],
        "data-v-53dc1269{padding:20px}\n.",
        [1],
        "policy-title.",
        [1],
        "data-v-53dc1269{font-size:24px;margin-bottom:20px;text-align:center}\n.",
        [1],
        "section-title.",
        [1],
        "data-v-53dc1269{font-size:18px;margin-top:20px}\n.",
        [1],
        "section-text.",
        [1],
        "data-v-53dc1269{line-height:1.6;margin-bottom:10px}\n.",
        [1],
        "update-info.",
        [1],
        "data-v-53dc1269{margin-top:10px;text-align:right}\n",
      ],
      undefined,
      { path: "./subpackages/apppolicy/privacyPolicy/privacyPolicy.wxss" }
    );
}
