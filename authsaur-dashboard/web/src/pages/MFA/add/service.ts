import { request } from 'umi';

export async function fakeSubmitForm(params: any) {
  return request('/api/service', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitUpdateForm(params: any) {
  return request('/api/service', {
    method: 'PUT',
    data: params,
  });
}

export async function queryFakeItem(params: any) {
  return request(`/api/service/${params.id}`, {
    method: 'GET',
  });
}
export async function removeFakeItem(params: any) {
  return request(`/api/service/${params.id}`, {
    method: 'DELETE',
  });
}
