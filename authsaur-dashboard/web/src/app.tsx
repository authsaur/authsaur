import Footer from '@/components/Footer';
import RightContent from '@/components/RightContent';
import type { Settings as LayoutSettings } from '@ant-design/pro-layout';
import { PageLoading, SettingDrawer } from '@ant-design/pro-layout';
import { notification } from 'antd';
import type { RequestConfig, RunTimeLayoutConfig } from 'umi';
import { history } from 'umi';
import defaultSettings from '../config/defaultSettings';
import logo from '../public/casing.png';
import { currentUser as queryCurrentUser } from './services/ant-design-pro/api';

// const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';

/** 获取用户信息比较慢的时候会展示一个 loading */
export const initialStateConfig = {
  loading: <PageLoading />,
};

/**
 * @see  https://umijs.org/zh-CN/plugins/plugin-initial-state
 * */
export async function getInitialState(): Promise<{
  settings?: Partial<LayoutSettings>;
  currentUser?: API.CurrentUser;
  loading?: boolean;
  fetchUserInfo?: () => Promise<API.CurrentUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    try {
      const msg = await queryCurrentUser();
      return msg.data;
    } catch (error) {
      //history.push(loginPath);
    }
    return {};
  };
  // 如果不是登录页面，执行
  if (history.location.pathname.indexOf('/user/login') == -1 && history.location.pathname.indexOf('/user/cas') == -1) {
    const currentUser = await fetchUserInfo();
    return {
      fetchUserInfo,
      currentUser,
      settings: defaultSettings,
    };
  }
  return {
    fetchUserInfo,
    currentUser:{},
    settings: defaultSettings,
  };
}

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({ initialState, setInitialState }) => {
  return {
    logo: logo,
    // menu: { defaultOpenAll: true, ignoreFlatMenu: true },
    menu: { ignoreFlatMenu: true },
    // menuExtraRender:()=>(<Search placeholder="请输入" />),
    rightContentRender: () => <RightContent />,
    // headerContentRender:(props) => {console.log(props); return <div>管理控制台 </div>},
    disableContentMargin: false,
    // waterMarkProps: {
    //   content: initialState?.currentUser?.name,
    // },
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { location } = history;
      // 如果没有登录，重定向到 login
      if (!initialState?.currentUser && location.pathname !== loginPath) {
        history.push(loginPath);
      }
    },
    // links: isDev
    //   ? [
    //     // <span>Casing V7.0.0-dev</span>,
    //     <AvatarDropdown />
    //   ]
    //   : [],
    // links: [
    //     <>
    //         <LogoutOutlined />
    //         <span><AvatarDropdown /></span>
            
    //       </>,
    //       // <Link key="openapi" to="/umi/plugin/openapi" target="_blank">
    //       //   <LinkOutlined />
    //       //   <span>OpenAPI 文档</span>
    //       // </Link>,
    //       // <Link to="/~docs" key="docs">
    //       //   <BookOutlined />
    //       //   <span>业务组件文档</span>
    //       // </Link>,
    //     ],
    menuHeaderRender: undefined,
    // itemRender: (route, params, routes, paths) => {
    //   console.log(route, routes)
    //   console.log(paths)
    //   const first = routes.indexOf(route) === 0;
    //   const showlink = routes.length >= 3 && (paths.length == routes.length - 2);
    //   if (first) {
    //     return <Link to="/">首页</Link>
    //   } else if (showlink) {
    //     return <Link to={route.path}>{route.breadcrumbName}</Link>
    //   } else {
    //     //return <Link to={route.path}>{route.breadcrumbName}</Link>
    //     return <span>{route.breadcrumbName}</span>
    //   }
    // },
    // breadcrumbRender: (routers = []) => [
    //   {
    //     path: '/',
    //     breadcrumbName: '首页',
    //   },
    //   ...routers,
    // ],
    // 自定义 403 页面
    // unAccessible: <div>unAccessible</div>,
    // 增加一个 loading 的状态
    childrenRender: (children, props) => {
      // if (initialState?.loading) return <PageLoading />;
      return (
        <>
          {children}
          {(
            <SettingDrawer
              disableUrlParams
              enableDarkTheme
              settings={initialState?.settings}
              onSettingChange={(settings) => {
                setInitialState((preInitialState) => ({
                  ...preInitialState,
                  settings,
                }));
              }}
            />
          )}
        </>
      );
    },
    ...initialState?.settings,
  };
};

const codeMessage = {
  200: '服务器成功返回请求的数据。',
  201: '新建或修改数据成功。',
  202: '一个请求已经进入后台排队（异步任务）。',
  204: '删除数据成功。',
  400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  401: '用户没有权限（令牌、用户名、密码错误）。',
  403: '用户得到授权，但是访问是被禁止的。',
  404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  405: '请求方法不被允许。',
  406: '请求的格式不可得。',
  410: '请求的资源被永久删除，且不会再得到的。',
  422: '当创建一个对象时，发生一个验证错误。',
  500: '服务器发生错误，请检查服务器。',
  502: '请求错误。',
  503: '服务不可用，服务器暂时过载或维护。',
  504: '请求超时。',
};

const key = "globalkey";
/** 异常处理程序
 * @see https://beta-pro.ant.design/docs/request-cn
 */
/**
 * 异常处理程序
 */
const errorHandler = (error: { response: Response; data: any }): Promise<Response> => {
  console.log(error);
  const isLoginPage = window.location.pathname.indexOf('/user/login') !== -1;
  const { response } = error;
  if (error && response && response.status) {
    if (([401].includes(response.status) || error?.data?.code == 401) && !isLoginPage) {
      localStorage.removeItem('token');
      history.replace(`/user/login?redirect=${encodeURIComponent(window.location.pathname)}`);
      return Promise.reject(response);
    }
    if ([401].includes(response.status) && isLoginPage) return Promise.reject(response);

    // TODO: improve code message mapper
    const errorText = error.data?.message || codeMessage[response.status];
    const errorCode = error.data?.code || response.status;
    notification.error({
      key,
      message: `${errorText}`,
      duration: 1
      // message: `Request Error Code: ${errorCode}`,
      // description: errorText,
    });
  } else if (!response) {

    if (error?.type == 'AbortError') return Promise.reject(response);
    notification.error({
      key,
      description: 'Network Error',
      message: '',
      duration: 1
    });
  }
  // throw error;
  return Promise.reject(response);
};

// https://umijs.org/zh-CN/plugins/plugin-request
export const request: RequestConfig = {
  prefix: '',
  errorHandler,
  credentials: 'same-origin',
  requestInterceptors: [
    (url, options) => {
      const newOptions = { ...options };
      newOptions.headers = {
        ...options.headers,
        Authorization: localStorage.getItem('token') || '',
      };
      return {
        url,
        options: { ...newOptions, interceptors: true },
      };
    },
  ],
  // responseInterceptors: [
  //   async (res) => {
  //     // console.log(res)
  //     // if (res.status == 504) {
  //     //   return Promise.reject({ response: res });
  //     // }
  //     if (!res.ok) {
  //       // NOTE: http code >= 400, using errorHandler
  //       return res;
  //     }

  //     const data = await res.json();
  //     const { code = -1, success = false } = data as Res<any>;
  //     if (!success) {
  //       // eslint-disable-next-line
  //       return Promise.reject({ response: res, data });
  //     }
  //     return data;
  //   },
  // ],
};

