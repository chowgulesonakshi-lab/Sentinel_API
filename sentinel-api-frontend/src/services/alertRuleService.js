import axios from "axios";

const API_URL = "http://localhost:8080/alert-rules";

const getAuthHeader = () => ({
    headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
});

export const getAlertRules = async () => {
    const response = await axios.get(API_URL, getAuthHeader());
    return response.data;
};

export const getAlertRuleById = async (id) => {
    const response = await axios.get(`${API_URL}/${id}`, getAuthHeader());
    return response.data;
};

export const getRulesByEndpoint = async (endpointId) => {
    const response = await axios.get(`${API_URL}/endpoint/${endpointId}`, getAuthHeader());
    return response.data;
};

export const getRulesByService = async (serviceId) => {
    const response = await axios.get(`${API_URL}/service/${serviceId}`, getAuthHeader());
    return response.data;
};

export const createAlertRule = async (rule) => {
    const response = await axios.post(API_URL, rule, getAuthHeader());
    return response.data;
};

export const updateAlertRule = async (id, rule) => {
    const response = await axios.put(`${API_URL}/${id}`, rule, getAuthHeader());
    return response.data;
};

export const deleteAlertRule = async (id) => {
    await axios.delete(`${API_URL}/${id}`, getAuthHeader());
};

export const toggleAlertRule = async (id) => {
    const response = await axios.patch(`${API_URL}/${id}/toggle`, {}, getAuthHeader());
    return response.data;
};
