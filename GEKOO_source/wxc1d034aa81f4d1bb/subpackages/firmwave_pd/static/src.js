var e = require("../static/px_src1.js"),
  r = require("../static/px_src2.js");
module.exports = {
  get_hex_src: function (_) {
    return 0 == _
      ? e.get_hex_src(0)
      : 1 == _
      ? e.get_hex_src(1)
      : 2 == _
      ? e.get_hex_src(2)
      : 3 == _
      ? e.get_hex_src(3)
      : 4 == _
      ? r.get_hex_src(0)
      : 5 == _
      ? r.get_hex_src(1)
      : 6 == _
      ? r.get_hex_src(2)
      : 7 == _
      ? r.get_hex_src(3)
      : null;
  },
};
