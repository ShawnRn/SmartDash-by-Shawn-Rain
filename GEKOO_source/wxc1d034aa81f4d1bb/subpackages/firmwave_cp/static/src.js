var e = require("../static/cp_src1.js"),
  _ = require("../static/cp_src2.js");
module.exports = {
  get_hex_src: function (r) {
    return 0 == r
      ? e.get_hex_src(0)
      : 1 == r
      ? e.get_hex_src(1)
      : 2 == r
      ? e.get_hex_src(2)
      : 3 == r
      ? _.get_hex_src(0)
      : 4 == r
      ? _.get_hex_src(1)
      : 5 == r
      ? _.get_hex_src(2)
      : 6 == r
      ? _.get_hex_src(3)
      : 7 == r
      ? _.get_hex_src(4)
      : 8 == r
      ? e.get_hex_src(3)
      : 9 == r
      ? _.get_hex_src(5)
      : 10 == r
      ? e.get_hex_src(4)
      : null;
  },
};
