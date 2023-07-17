import { request } from 'umi';

export async function fakeSubmitForm(params: any) {
  return request('/api/authenticator/saveOrUpdateLdap', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitUpdateForm(params: any) {
  return request('/api/authenticator/saveOrUpdateLdap', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitUpdateFormDingTalk(params: any) {
  return request('/api/authenticator/saveOrUpdateDingTalk', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitFormDingTalk(params: any) {
  return request('/api/authenticator/saveOrUpdateDingTalk', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitUpdateFormRadius(params: any) {
  return request('/api/authenticator/saveOrUpdateRadius', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitFormRadius(params: any) {
  return request('/api/authenticator/saveOrUpdateRadius', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitFormOTP(params: any) {
  return request('/api/authenticator/saveOrUpdateOTP', {
    method: 'POST',
    data: params,
  });
}

export async function queryFakeItem(params: any) {
  return request(`/api/authenticator/${params.id}`, {
    method: 'GET',
  });
}
export async function removeFakeItem(params: any) {
  return request(`/api/authenticator/${params.id}`, {
    method: 'DELETE',
  });
}
export async function stateFakeItem(params: any) {
  return request(`/api/authenticator/${params.id}`, {
    method: 'PUT',
    data: {
      enabled: params?.enabled
    },
  });
}
export async function queryFakeList(params: {
  count: number;
}): Promise<{ data: { list: any[] } }> {
  return request('/api/authenticator', {
    params,
  });
}
export async function queryFakeUserList(params: {
  count: number;
}): Promise<{ data: { list: any[] } }> {
  return request('/api/usermanagement/search', {
    params,
  });
}
