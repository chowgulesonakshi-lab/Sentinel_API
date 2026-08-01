import axios from "axios";

const API = "http://localhost:8080";

export const getAlerts = async () => {
    const response = await axios.get(`${API}/alerts`);
    return response.data;
};