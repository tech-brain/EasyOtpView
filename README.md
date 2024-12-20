EasyOtpView Library
EasyOtpView is a customizable and easy-to-use Android library designed to simplify the implementation of OTP (One-Time Password) input fields. It provides a flexible solution to create OTP input views with various configurations, including support for multiple item count, custom styles, cursor visibility, masking characters, and RTL (Right-to-Left) text direction.

With EasyOtpView, you can easily integrate an OTP input interface into your Android app, improving user experience by offering smooth animations, dynamic styling, and an intuitive interface for OTP entry.

### How to
## To get a Git project into your build:

## Step 1. Add the JitPack repository to your build file

## Add it in your root build.gradle at the end of repositories:

dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}


 ## Step 2. Add the dependency

 dependencies {
	        implementation 'com.github.tech-brain:EasyOtpView:1.0.0'
	}




