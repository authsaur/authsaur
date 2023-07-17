import { PageContainer } from '@ant-design/pro-layout';
import { Card } from 'antd';
import TabsMember from './TabsMember';


const CardList = () => {
  return (
    <PageContainer>
      <Card>
        <TabsMember />
      </Card>
    </PageContainer>
  );
};

export default CardList;
