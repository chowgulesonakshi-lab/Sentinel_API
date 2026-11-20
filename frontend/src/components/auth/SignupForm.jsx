import { useState } from "react";
import { signup } from "../../services/authService";

function SignupForm({ setIsLogin }) {
    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
    });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState("error");

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((current) => ({ ...current, [name]: value }));
        if (message) setMessage("");
    };

    const handleSignup = async (event) => {
        event.preventDefault();
        if (loading) return;

        const payload = {
            name: formData.name.trim(),
            email: formData.email.trim().toLowerCase(),
            password: formData.password,
        };

        if (!payload.name || !payload.email || !payload.password) {
            setMessageType("error");
            setMessage("Please complete all fields.");
            return;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) {
            setMessageType("error");
            setMessage("Please enter a valid email address.");
            return;
        }
        if (payload.password.length < 8) {
            setMessageType("error");
            setMessage("Password must be at least 8 characters long.");
            return;
        }

        setLoading(true);
        setMessage("");

        try {
            const response = await signup(payload);
            const resultMessage = response.data?.message || "";

            if (response.status >= 200 && response.status < 300 &&
                (resultMessage === "Success" || !resultMessage)) {
                setMessageType("success");
                setMessage("Account created successfully. Please sign in with your new account.");
                setFormData({ name: "", email: "", password: "" });
                window.setTimeout(() => setIsLogin(true), 1400);
            } else {
                setMessageType("error");
                setMessage(resultMessage.replace(/^Error:\s*/, "") || "We couldn't create your account. Please try again.");
            }
        } catch (error) {
            const serverMessage =
                error.response?.data?.message ||
                error.response?.data?.error ||
                error.message;
            setMessageType("error");
            setMessage(
                serverMessage
                    ? serverMessage.replace(/^Error:\s*/, "")
                    : "Unable to reach the server. Please check your connection and try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md">
            <h2 className="text-4xl font-bold text-slate-900 mb-3">
                Create Account
            </h2>
            <p className="text-slate-500 mb-8">
                Join Sentinel API today.
            </p>

            <form onSubmit={handleSignup} noValidate>
                <label htmlFor="signup-name" className="block text-sm font-medium text-slate-700 mb-2">
                    Name
                </label>
                <input
                    id="signup-name"
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Enter your name"
                    autoComplete="name"
                    required
                    maxLength={100}
                    className="w-full border border-slate-300 rounded-lg p-3 mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />

                <label htmlFor="signup-email" className="block text-sm font-medium text-slate-700 mb-2">
                    Email
                </label>
                <input
                    id="signup-email"
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="Enter your email"
                    autoComplete="email"
                    required
                    className="w-full border border-slate-300 rounded-lg p-3 mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />

                <label htmlFor="signup-password" className="block text-sm font-medium text-slate-700 mb-2">
                    Password
                </label>
                <input
                    id="signup-password"
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Create a password"
                    autoComplete="new-password"
                    required
                    minLength={8}
                    className="w-full border border-slate-300 rounded-lg p-3 mb-6 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />

                {message && (
                    <div
                        role={messageType === "error" ? "alert" : "status"}
                        aria-live="polite"
                        className={`mb-4 rounded-lg border px-4 py-3 text-sm ${
                            messageType === "success"
                                ? "border-green-200 bg-green-50 text-green-800"
                                : "border-red-200 bg-red-50 text-red-700"
                        }`}
                    >
                        {message}
                    </div>
                )}

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white py-3 rounded-lg font-medium hover:bg-blue-700 transition disabled:cursor-not-allowed disabled:opacity-60"
                >
                    {loading ? "Creating account..." : "Create Account"}
                </button>
            </form>

            <p className="text-center text-sm text-slate-500 mt-6">
                Already have an account?
                <button
                    type="button"
                    onClick={() => setIsLogin(true)}
                    className="text-blue-600 hover:text-blue-700 ml-1 font-medium"
                >
                    Sign In →
                </button>
            </p>
        </div>
    );
}

export default SignupForm;
