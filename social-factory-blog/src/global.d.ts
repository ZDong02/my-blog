// Global TypeScript declarations for the application

declare global {
  interface HTMLElement {
    value?: string;
    files?: FileList | null;
    src?: string;
    alt?: string;
    disabled?: boolean;
    checked?: boolean;
    selectionStart?: number | null;
    selectionEnd?: number | null;
  }

  interface Element {
    dataset?: DOMStringMap;
  }

  interface EventTarget {
    [key: string]: any;
    value?: string;
    files?: FileList | null;
    usernameOrEmail?: { value?: string };
    password?: { value?: string };
    closest?(selector: string): Element | null;
  }

  interface Window {
    [key: string]: any;

    // Auth manager for handling authentication state
    authManager: {
      isAuthenticated: boolean;
      isLoading: boolean;
      currentUser: any;
      init(): Promise<void>;
      login(credentials: any): Promise<any>;
      logout(): Promise<void> | void;
      getAuthState(): { user: any; isAuthenticated: boolean; isLoading: boolean };
      addListener(listener: (state: any) => void): void;
    };

    // Toast notification functions
    showToast(message: string, type?: 'success' | 'error' | 'warning' | 'info', duration?: number): void;
    showError(message: string): void;
    showSuccess(message: string): void;
    showWarning(message: string): void;
    showInfo(message: string): void;

    // API client
    apiClient: any;
  }
}

export {};
