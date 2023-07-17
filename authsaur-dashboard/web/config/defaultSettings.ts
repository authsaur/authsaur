import { Settings as LayoutSettings } from '@ant-design/pro-layout';

const Settings: LayoutSettings & {
  pwa?: boolean;
  logo?: string;
} = {
  "title": "Authsaur控制台",
  "navTheme": "dark",
  "primaryColor": "#3f51b5",
  "layout": "side",
  "contentWidth": "Fluid",
  "fixedHeader": false,
  "fixSiderbar": true,
  "pwa": false,
  "headerHeight": 48,
  "splitMenus": false,
  // "headerRender": false,
  "footerRender": false,
};
  
  

export default Settings;
