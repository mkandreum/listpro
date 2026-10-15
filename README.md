<div align="center">

# 🛒 ListPro

**Catálogo de productos y lista de la compra, 100 % offline**

*Un producto de [Xyon Platforms](https://github.com/mkandreum)*

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![WebView](https://img.shields.io/badge/WebView-4285F4?style=flat&logo=googlechrome&logoColor=white)

</div>

---

## ¿Qué es ListPro?

ListPro es una app Android que monta un catálogo de productos sobre una WebView con una interfaz web propia (`assets/index.html`). El catálogo se sirve desde un CSV empaquetado en la app y se entrega a la interfaz por trozos mediante un puente JavaScript, de modo que se navegaba sin bloquear la UI aunque el catálogo tenga cientos de productos. Sin servidores: todo vive en el dispositivo.

## ✨ Funcionalidades

- **Catálogo completo**: `products_macro.csv` con id, categoría, nombre, marca, precio e imagen de cada producto
- **Carga por trozos**: el WebView pide productos en páginas (`getProductChunk`) y la app los convierte a JSON
- **Interfaz rica**: tema propio, tipografía Poppins y gráficas con Chart.js
- **Compartir por NFC**: un móvil emula una tarjeta NFC (Host Card Emulation) con el contenido y el otro la lee para unirse
- **Copias de seguridad**: guarda el estado en `listpro_backup.json` y restáuralo desde un archivo
- **100 % offline**: el catálogo y la interfaz van dentro del APK

## 🛠️ Stack técnico

| Capa | Tecnología |
|------|----------|
| Lenguaje | Kotlin (interfaz nativa mínima) |
| Interfaz | WebView + puente `@JavascriptInterface` (`Android`) |
| Datos | CSV empaquetado en `assets/` parseado a JSON |
| NFC | `HostApduService` (AID `F0010203040506`) con respuesta NDEF de texto |
| Gráficos | Chart.js (CDN) |
| Licencia | Google Play License Check (pairip) |

minSdk 27, targetSdk 36.

## 📁 Estructura del repositorio

Repositorio de **análisis** del APK descompilado, no del proyecto Android original:

```
01_zip/      Extracto crudo del APK (classes.dex, assets, META-INF)
02_apktool/  apktool: AndroidManifest.xml decodificado, smali/ y res/
03_jadx/     JADX: fuentes Java decompiladas + recursos
RESUMEN.txt  Inventario de archivos del descompilado
```

### Cómo se regenera

```bash
unzip com.listpro.xyon.pro-2.apk -d 01_zip
apktool d com.listpro.xyon.pro-2.apk -o 02_apktool
jadx -d 03_jadx com.listpro.xyon.pro-2.apk
```

## 🏢 Xyon Platforms

ListPro es un producto desarrollado y mantenido por **Xyon Platforms**, empresa especializada en soluciones digitales para negocios locales y pymes.

> © Xyon Platforms — Todos los derechos reservados
