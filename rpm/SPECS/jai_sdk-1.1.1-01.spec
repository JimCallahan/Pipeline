Summary: Java Advanced Imaging API
Name: jai_sdk
Version: 1.1.1
Release: 01
Copyright: public domain
Group: Development/Libraries
Source0: jai-1_1_1_01-lib-linux-i586-jdk.bin
Source1: jai_imageio-1_0-lib-linux-i586.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-root
URL: http://java.sun.com/products/java-media/jai/downloads/download-1_1_2.html
Packager: Jim Callahan <jim@polaris.net>


%description
The JavaTM Advanced Imaging application programming interface (API) enables 
developers to easily incorporate high-performance, network-enabled, scalable, 
platform-independent image processing into Java technology-based applications 
and applets. By using the inherent stengths of the Java language, Java 
Advanced Imaging extends the concept of "Write Once, Run AnywhereTM" to image 
processing applications.

The Java Advanced Imaging-Image I/O Tools package set provides reader, writer, 
and stream plug-ins for the Java Image I/O Framework and Image I/O-based read 
and write operations for Java Advanced Imaging. Reader-writer plug-ins are 
supplied for the BMP, JPEG, JPEG 2000, PNG, PNM, Raw, TIFF, and WBMP image 
formats. The supplied streams and associated service providers use the New I/O 
APIs.


%prep
%setup -n jai_imageio_1.0 -c jai_imageio_1.0 -T -a 1

%build


%install

# JAI
mkdir -p $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/i386
install -m 755 libclib_jiio.so $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/i386

mkdir -p $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/ext
install -m 644 clibwrapper_jiio.jar $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/ext
install -m 644 jai_imageio.jar      $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/ext
install -m 644 mlibwrapper_jai.jar  $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/ext

mkdir -p $RPM_BUILD_ROOT/usr/share/doc/jai_imageio-1.0
install -m 644 LICENSE-jai_imageio.txt   $RPM_BUILD_ROOT/usr/share/doc/jai_imageio-1.0
install -m 644 README-jai_imageio.html   $RPM_BUILD_ROOT/usr/share/doc/jai_imageio-1.0
install -m 644 COPYRIGHT-jai_imageio.txt $RPM_BUILD_ROOT/usr/share/doc/jai_imageio-1.0

# JAI IMAGE I/O TOOLS
mkdir -p $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/ext
mkdir -p $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2/jre/lib/i386

cd $RPM_BUILD_ROOT/usr/java/j2sdk1.4.2
env MORE=true sh /usr/src/redhat/SOURCES/jai-1_1_1_01-lib-linux-i586-jdk.bin <<EOF
yes
EOF


%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/usr/java/j2sdk1.4.2/jre/lib/i386/libclib_jiio.so  
/usr/java/j2sdk1.4.2/jre/lib/ext/clibwrapper_jiio.jar
/usr/java/j2sdk1.4.2/jre/lib/ext/jai_imageio.jar  
/usr/java/j2sdk1.4.2/jre/lib/ext/mlibwrapper_jai.jar
/usr/share/doc/jai_imageio-1.0/LICENSE-jai_imageio.txt  
/usr/share/doc/jai_imageio-1.0/README-jai_imageio.html
/usr/share/doc/jai_imageio-1.0/COPYRIGHT-jai_imageio.txt  

/usr/java/j2sdk1.4.2/jre/lib/ext/jai_core.jar
/usr/java/j2sdk1.4.2/jre/lib/ext/jai_codec.jar
/usr/java/j2sdk1.4.2/jre/lib/ext/mlibwrapper_jai.jar
/usr/java/j2sdk1.4.2/jre/lib/i386/libmlib_jai.so
/usr/java/j2sdk1.4.2/COPYRIGHT-jai.txt
/usr/java/j2sdk1.4.2/INSTALL-jai.txt
/usr/java/j2sdk1.4.2/LICENSE-jai.txt
/usr/java/j2sdk1.4.2/README-jai.txt
/usr/java/j2sdk1.4.2/UNINSTALL-jai

%changelog
* Fri Nov  2 2001 Jim Callahan <jim@polaris.net>
- Initial build.


