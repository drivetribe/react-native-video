const blacklist = require('metro-config/src/defaults/blacklist');
const path = require('path');
const cwd = path.resolve(__dirname);

function getBlacklist() {
  return blacklist([
    /@drivetribe[/\\]react-native-video[/\\]example[/\\]node_modules[/\\]react-native[/\\].*/,
  ]);
}

module.exports = {
  transformer: {
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: false,
      },
    }),
  },
  resolver: {
    blacklistRE: getBlacklist(),
    extraNodeModules: {
      'react-native': path.resolve(cwd, './node_modules/react-native'),
    },
  },
};
