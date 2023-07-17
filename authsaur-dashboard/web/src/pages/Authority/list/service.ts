import { request } from 'umi';
import type { CardListItemDataType } from './data.d';

export async function queryFakeList(params: {
  count: number;
}): Promise<{ data: { list: CardListItemDataType[] } }> {
  return request('/api/service', {
    params,
  });
}

export async function queryFakeItem(params: any) {
  return request(`/api/service_strategy/${params.id}`, {
    method: 'GET',
  });
}
export async function fakeSubmitForm(params: any) {
  return request('/api/service_strategy', {
    method: 'POST',
    data: params,
  });
}