package ab1;

import ab1.impl.gruppe_29_prett_fischer_szolderits.NFAFactoryImpl;

public class NFAProvider {
    public static NFAFactory provideFactory() {
        return new NFAFactoryImpl();
    }
}
