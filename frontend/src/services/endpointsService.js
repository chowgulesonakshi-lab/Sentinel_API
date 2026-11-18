import api from "../api/axios";

export const getEndpoints = async () => {
    const response = await api.get("/endpoints");
    return response.data;
};

export const createEndpoint = async (endpoint) => {
    const response = await api.post("/endpoints", endpoint);
    return response.data;
};

export const updateEndpoint = async (id, endpoint) => {
    const response = await api.put(`/endpoints/${id}`, endpoint);
    return response.data;
};

export const deleteEndpoint = async (id) => {
    await api.delete(`/endpoints/${id}`);
};

export const triggerEndpointCheck = async (id) => {
    const response = await api.post(`/endpoints/${id}/trigger`, {});
    return response.data;
};
