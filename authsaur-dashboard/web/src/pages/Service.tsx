import { PageHeaderWrapper } from '@ant-design/pro-layout';
import { Card } from 'antd';
import React from 'react';
import { useIntl } from 'umi';

const Admin: React.FC = () => {
  const intl = useIntl();
  return (
    <PageHeaderWrapper
      title={<></>}
      // content={intl.formatMessage({
      //   id: 'pages.admin.subPage.title',
      //   defaultMessage: 'This page can only be viewed by admin',
      // })}
    >
      <Card>
        
      </Card>
    </PageHeaderWrapper>
  );
};

export default Admin;
