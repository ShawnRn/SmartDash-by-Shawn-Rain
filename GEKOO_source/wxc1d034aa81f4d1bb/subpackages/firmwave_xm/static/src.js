var e = require("../static/xm_src1.js"),
  _ = require("../static/xm_src2.js");
module.exports = {
  get_hex_src: function (r) {
    return 0 == r
      ? e.get_hex_src(0)
      : 1 == r
      ? e.get_hex_src(1)
      : 2 == r
      ? e.get_hex_src(2)
      : 3 == r
      ? e.get_hex_src(3)
      : 4 == r
      ? _.get_hex_src(0)
      : 5 == r
      ? _.get_hex_src(1)
      : 6 == r
      ? _.get_hex_src(2)
      : 7 == r
      ? _.get_hex_src(3)
      : 8 == r
      ? _.get_hex_src(4)
      : 9 == r
      ? _.get_hex_src(5)
      : null;
  },
};
