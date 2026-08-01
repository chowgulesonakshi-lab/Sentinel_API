import { useState } from "react";

import BrandPanel from "../components/auth/BrandPanel";
import LoginForm from "../components/auth/LoginForm";
import SignupForm from "../components/auth/SignupForm";

function Auth() {
    const [isLogin, setIsLogin] = useState(true);
    return (
        <div className="min-h-screen flex bg-slate-100">
            <BrandPanel />
            <div className="flex w-full md:w-1/2 justify-center items-center p-8">
                {
                    isLogin
                    ?
                    <LoginForm setIsLogin={setIsLogin} />
                    :
                    <SignupForm setIsLogin={setIsLogin} />
                }
            </div>
        </div>
    );
}

export default Auth;