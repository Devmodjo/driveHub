import packageInfo from '../../package.json';

const basePath = 'http://localhost:8075';

export const environment = {
  appVersion: packageInfo.version,
  production: false,
  basePath,
  ssk: 'we-sv-rtw-gw-$%-äsdf',
  ssv: 'öer:gerg-regwe=?w-_a@,'
};
