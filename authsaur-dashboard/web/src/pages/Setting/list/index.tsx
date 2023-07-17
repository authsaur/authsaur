import { CheckCard } from '@ant-design/pro-components';
import ProForm, {
  ProFormDigit,
  ProFormInstance,
  ProFormItem,
  ProFormSwitch,
  ProFormText
} from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Button, Card, message } from 'antd';
import React, { useRef } from 'react';
import { fakeSubmitForm, queryFakeList } from './service';

const CardList = () => {
  const formLoginRef = useRef<ProFormInstance>();
  const formConsoleRef = useRef<ProFormInstance>();

  React.useEffect(() => {
    queryFakeList({ keys: ['authsaur.safe', 'authsaur.console'] }).then(function (resp) {
      console.log(resp);
      const response = resp?.data || {};
      console.log(response);

      formLoginRef?.current?.setFieldsValue({
        ...response['authsaur.safe'],
      });
      formConsoleRef?.current?.setFieldsValue({
        ...response['authsaur.console'],
        slo: response['authsaur.console']?.slo == 'true',
      });
    });
  }, []);

  return (
    <PageContainer>
      {/* <div style={{height:'24px'}}></div> */}
      {/* <Card bordered={false}> */}
      <Card title="系统配置" >
        <ProForm<{
          name: string;
          company?: string;
          useMode?: string;
        }>
          submitter={{
            render: (props, doms) => {
              return [
                <Button type="primary" key="submit" onClick={() => props.form?.submit?.()}>
                  保存设置
                </Button>,
              ];
            },
          }}
          onFinish={async (values) => {
            console.log(values);
            if (values.maxTimeToLiveInSeconds <= values.timeToKillInSeconds) {
              message.error({
                content: '会话最长存活时间不能小于最长空闲时间',
                className: 'box_error',
              });
              return false;
            }
            const response = await fakeSubmitForm({ key: 'authsaur.safe', value: values });
            if (response.success == true) {
              message.success({ content: '更新成功', className: 'box_success' });
              return true;
            } else {
              message.error({ content: '更新失败:' + response?.msg, className: 'box_error' });
              return false;
            }
          }}
          formRef={formLoginRef}
          autoFocusFirstInput
        >
          <ProFormItem hidden label="用户名唯一性" name="principalPolicy">
            <CheckCard.Group style={{ width: '100%' }}>
              <CheckCard
                title="用户名全局唯一"
                // avatar={
                //   <Avatar
                //     src="/center.svg"
                //     size="large"
                //   />
                // }
                description="用户名标识唯一用户"
                value="global"
                checked
              />
              <CheckCard
                title="用户名认证器内唯一"
                // avatar={
                //   <Avatar
                //     src="/center-bg.svg"
                //     size="large"
                //   />
                // }
                description="用ID标识唯一用户，规则：用户名 + 哈希"
                value="auth"
              />
            </CheckCard.Group>
          </ProFormItem>
          <ProFormText
            label="用户密码正则表达式"
            width="xl"
            name="passwordPolicyPattern"
            allowClear={false}
          />
          <ProFormDigit
            label="会话最长存活时间"
            width="sm"
            name="maxTimeToLiveInSeconds"
            fieldProps={{
              addonAfter: '秒',
              controls: false,
              precision: 0,
            }}
          />
          <ProFormDigit
            label="会话最长空闲时间"
            tooltip="超过时间段内不活跃将销毁会话"
            width="sm"
            name="timeToKillInSeconds"
            fieldProps={{
              addonAfter: '秒',
              controls: false,
              precision: 0,
            }}
          />
          {/* <Divider orientation="left">日志</Divider> */}
          <ProFormDigit
            label="日志保存天数"
            width="sm"
            name="auditMaxAgeDay"
            min={1}
            fieldProps={{
              addonAfter: '天',
              controls: false,
              precision: 0,
            }}
          />
        </ProForm>
      </Card>
      <Card title="管理后台" style={{ marginTop: '24px' }}>
        <ProForm<{
          name: string;
          company?: string;
          useMode?: string;
        }>
          submitter={{
            render: (props, doms) => {
              return [
                <Button type="primary" key="submit" onClick={() => props.form?.submit?.()}>
                  保存设置
                </Button>,
              ];
            },
          }}
          onFinish={async (values) => {
            console.log(values);
            const response = await fakeSubmitForm({ key: 'authsaur.console', value: values });
            if (response.success == true) {
              message.success({ content: '更新成功', className: 'box_success' });
              return true;
            } else {
              message.error({ content: '更新失败:' + response?.msg, className: 'box_error' });
              return false;
            }
          }}
          formRef={formConsoleRef}
          autoFocusFirstInput
        >
          <ProFormSwitch
            label="单点退出："
            width="xl"
            name="slo"
            // initialValue={1}
          />
        </ProForm>
      </Card>
      {/* </Card> */}
    </PageContainer>
  );
};

export default CardList;
