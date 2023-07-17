import { message, notification, Spin } from 'antd';
import type React from 'react';
import { useEffect } from 'react';
import { request, history, useModel } from 'umi';


const getUrlQuery: (key: string) => string | false = (key: string) => {
    const query = window.location.search.substring(1);
    const vars = query.split('&');

    for (let i = 0; i < vars.length; i += 1) {
        const pair = vars[i].split('=');
        if (pair[0] === key) return pair[1];
    }
    return false;
};


const Page: React.FC = () => {

    const [messageApi, contextHolder] = message.useMessage();
    const { initialState, setInitialState } = useModel('@@initialState');

    const fetchUserInfo = async (userInfo) => {
        if (userInfo) {
            await setInitialState((s) => ({
                ...s,
                currentUser: userInfo,
            }));
        }
    };

    const code = getUrlQuery('ticket');
    // alert(code)
    console.log(code)

    useEffect(() => {
        try {
            const redirect = getUrlQuery('redirect');
            const goto = redirect ? ('?redirect=' + decodeURIComponent(redirect)) : '';
            const service = encodeURIComponent(window.location.origin + window.location.pathname + goto);
            request(`/api/login/cas?ticket=${code}&service=${service}`).then((result) => {
                if (!result?.success) {
                    notification.error({ message: result?.msg });
                    history.replace({
                        pathname: "/user/login",
                    });
                    return;
                }
                console.log(service)
                localStorage.setItem('token', result.data.token);
                // notification.success({
                //   message: formatMessage({ id: 'component.status.success' }),
                //   description: formatMessage({ id: 'component.user.loginMethodPassword.success' }),
                //   duration: 1,
                // });
                fetchUserInfo(result.data.user).then(() => {
                    const go = (redirect ? (decodeURIComponent(redirect)) : "/");
                    // window.location.pathname =  go;
                    // window.lcation.href
                    history.replace({
                        pathname: go,
                    });
                });
            }).catch(function (error) {
                console.log(error);
                history.replace({
                    pathname: "/user/login",
                });
            });

            // eslint-disable-next-line no-empty
        } catch (e) {
        }
    }, [code]);



    return <div style={{ display: 'flex', justifyContent: "center", alignItems: "center", height: 'inherit' }}><Spin></Spin></div>;
};

export default Page;
