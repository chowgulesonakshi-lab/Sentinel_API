import api from "../api/axios";

export const getAlertRules = async () => {
    const response = await api.get("/alert-rules");
    return response.data;
};

export const getAlertRuleById = async (id) => {
    const response = await api.get(`/alert-rules/${id}`);
    return response.data;
};

export const getRulesByEndpoint = async (endpointId) => {
    const response = await api.get(`/alert-rules/endpoint/${endpointId}`);
    return response.data;
};

export const getRulesByService = async (serviceId) => {
    const response = await api.get(`/alert-rules/service/${serviceId}`);
    return response.data;
};

export const createAlertRule = async (rule) => {
    const response = await api.post("/alert-rules", rule);
    return response.data;
};

export const updateAlertRule = async (id, rule) => {
    const response = await api.put(`/alert-rules/${id}`, rule);
    return response.data;
};

export const deleteAlertRule = async (id) => {
    await api.delete(`/alert-rules/${id}`);
};

export const toggleAlertRule = async (id) => {
    const response = await api.patch(`/alert-rules/${id}/toggle`, {});
    return response.data;
};
