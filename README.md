# Vaadin Open-Core Counter — Plain-Java Demo

A small but technically clean reference application that shows how to
build a Vaadin Flow product as an **Open-Core**:

- one **OSS / community** module that is a complete, runnable Vaadin
  app on its own,
- one **Enterprise** module that contributes additional views, routes,
  event listeners and a navbar badge on top — *without* the community
  module ever knowing it exists,
- **no Spring Boot**, **no Jakarta EE platform**, **no DI container**,
  **no users, no roles, no licence checks, no feature-flag service**.

The domain is intentionally trivial: a counter that goes up, down or
back to zero. The interesting part is **how the two modules are
wired** — that is what this repository demonstrates, and what the
companion blog post talks about.

---

## Table of contents

1. [What is "Open-Core" here?](#what-is-open-core-here)
2. [Architecture at a glance](#architecture-at-a-glance)
3. [Tech stack](#tech-stack)
4. [Repository layout](#repository-layout)
5. [Prerequisites](#prerequisites)
6. [Quick start](#quick-start)
7. [Running OSS mode](#running-oss-mode)
8. [Running Enterprise mode](#running-enterprise-mode)
9. [Tests](#tests)
10. [Mutation testing](#mutation-testing)
11. [How the extension mechanism actually works](#how-the-extension-mechanism-actually-works)
12. [Extending the demo with your own feature](#extending-the-demo-with-your-own-feature)
13. [Lessons learned — the traps we hit on the way](#lessons-learned--the-traps-we-hit-on-the-way)
14. [IntelliJ IDEA setup](#intellij-idea-setup)
15. [Acceptance summary](#acceptance-summary)

---

## What is "Open-Core" here?

> An Open-Core product ships its **core** as open-source software and
> sells an **Enterprise** edition that adds value on top: more
> features, more integrations, support, compliance bits, an SLA — the
> usual menu.

The architectural requirement is that those two pieces must stay
**cleanly separated** at the source level: the OSS module knows
nothing about the Enterprise module. They are joined at the
classpath, not in the code.

In this demo:

- The **OSS edition** ships a counter view and an about view.
- The **Enterprise edition** ships three additional views — `History`,
  `Audit Log`, `Export` — and two event listeners that record every
  counter change into in-memory stores. It also drops an
  "Enterprise Edition" pill into the navbar.
- The OSS and Enterprise sources live in two independent Maven
  projects. Neither has the other as a `<parent>`. The Enterprise
  jar **may** depend on the OSS jar; the OSS jar **must not** depend
  on Enterprise.
- The bridge between them is a single SPI:
  `ServiceLoader.load(FeatureContribution.class)`.

That is the whole trick. The rest is plumbing.

---

## Architecture at a glance

```
counter-community  (OSS — runnable on its own)
  ├─ launcher
  │    └─ CounterApplicationLauncher        (embedded Jetty main())
  ├─ app
  │    ├─ Application                       (static holder)
  │    ├─ ApplicationContext                (composition root)
  │    ├─ AppShell                          (Vaadin AppShellConfigurator)
  │    └─ OpenCoreRouteInitializer          (VaadinServiceInitListener)
  ├─ domain
  │    ├─ CounterAction                     (enum)
  │    ├─ CounterChangedEvent               (record)
  │    ├─ CounterState                      (mutable model)
  │    └─ CounterService                    (publishes events)
  ├─ extension                              ←  the SPI
  │    ├─ FeatureContribution               (interface)
  │    ├─ CounterEventFeature               (interface)
  │    ├─ CounterEventListener              (interface)
  │    ├─ RouteContribution                 (record)
  │    ├─ MenuContribution                  (record)
  │    ├─ NavbarContribution                (interface)
  │    └─ FeatureRegistry                   (ServiceLoader entry point)
  ├─ ui
  │    ├─ MainLayout                        (AppLayout, @Layout("/"))
  │    └─ core
  │         ├─ CounterView
  │         ├─ AboutView
  │         └─ CoreFeatureContribution      ← contributes OSS routes + menu
  └─ resources/META-INF/services
       ├─ com.svenruppert.opencore.counter.extension.FeatureContribution
       │    → CoreFeatureContribution
       └─ com.vaadin.flow.server.VaadinServiceInitListener
            → OpenCoreRouteInitializer

counter-enterprise (Enterprise extension)
  ├─ EnterpriseFeatureContribution          ← contributes Enterprise SPI
  ├─ history
  │    ├─ HistoryEntry, HistoryStore, HistoryCounterEventListener
  │    └─ HistoryView
  ├─ audit
  │    ├─ AuditEntry, AuditLogStore, AuditLogCounterEventListener
  │    └─ AuditLogView
  ├─ export
  │    ├─ ExportJsonBuilder
  │    └─ ExportView
  ├─ ui/components
  │    └─ EnterpriseEditionBadge            ← pill rendered in the navbar
  └─ resources/META-INF/services
       └─ com.svenruppert.opencore.counter.extension.FeatureContribution
            → EnterpriseFeatureContribution
```

**Dependency direction** — and the reason the demo is interesting:

```
counter-enterprise ────► counter-community
counter-community  ────► (nothing — never references the Enterprise side)
```

A boundary test (`CommunityDoesNotReferenceEnterpriseTest`) fails the
build if a community source file ever names `.counter.enterprise`,
`EnterpriseFeatureContribution`, `HistoryView`, `AuditLogView` or
`ExportView`. That test is the executable definition of "Open-Core
boundary" in this repo.

---

## Tech stack

| Layer            | Choice                                              | Why                                                                       |
|------------------|-----------------------------------------------------|---------------------------------------------------------------------------|
| Java             | 21 (compiled with `--release 21`)                   | LTS baseline; records + sealed types + pattern matching are used.         |
| Build            | Maven 4.0.0-rc-1 via `./mvnw`                       | Wrapper-pinned so IntelliJ and CI use the same version.                   |
| UI               | Vaadin Flow 25.1.1                                  | First version with the free `browserless-test-junit6` library.            |
| HTTP             | Embedded Jetty 12.1.8 (EE11)                        | Pure `main()` start, no external app server, no WAR.                      |
| Servlet API      | `jakarta.servlet-api:6.1.0` (compile scope)         | Forced to compile scope so `mvn exec:java` actually has it at runtime.    |
| JSON             | `tools.jackson.core:3.1.2` (pinned)                 | Vaadin 25.1 needs Jackson ≥ 3.1; Maven 3 picks 3.0.4 by default.          |
| Unit tests       | JUnit Jupiter 6.1.0-M1                              | Pulled transitively by browserless; coexists with Jupiter 5 if present.   |
| UI tests         | `com.vaadin:browserless-test-junit6:1.0.0`          | In-process Vaadin component tree, no browser, no Selenium.                |
| Mutation tests   | PIT 1.23.0 + `pitest-junit5-plugin:1.2.3`           | Used to audit assertion quality, not just line coverage.                  |
| Frontend         | Vite (via `vaadin-maven-plugin:25.1.1`)             | Production bundle is built once during `mvn install`.                     |
| Node             | auto-downloaded to `~/.vaadin/node-v24.14.1/`       | Comes in via the Vaadin Maven plugin; no manual Node install needed.      |

Explicit non-choices: no Spring, no Spring Boot, no CDI, no JPA, no
EJB, no JAX-RS, no Quarkus, no Micronaut, no Helidon, no Guice, no
Lombok, no annotation-driven role/permission framework.

---

## Repository layout

```
vaadin-opencore-counter/
├── pom.xml                       ← aggregator POM only, NOT a parent
├── mvnw, mvnw.cmd, .mvn/         ← Maven Wrapper at the workspace root
├── README.md
│
├── counter-community/
│   ├── pom.xml                   ← independent Maven project
│   ├── mvnw, mvnw.cmd, .mvn/     ← own wrapper so the module is standalone
│   └── src/{main,test}/
│
└── counter-enterprise/
    ├── pom.xml                   ← independent Maven project
    ├── mvnw, mvnw.cmd, .mvn/     ← own wrapper so the module is standalone
    └── src/{main,test}/
```

The root `pom.xml` is a pure aggregator. It does **not** declare
`<parent>` for the two modules. You can `cd counter-community &&
./mvnw install` without ever touching the root.

---

## Prerequisites

- **JDK 21+** (anything newer also works; the project itself is
  compiled with `--release 21`).
- **No local Maven required** — every command uses `./mvnw` which
  downloads Maven 4.0.0-rc-1 into `~/.m2/wrapper/dists/`.
- **No local Node required** — the `vaadin-maven-plugin` downloads
  Node into `~/.vaadin/node-v24.14.1/` on first build.
- An IDE that understands Maven (IntelliJ IDEA recommended).

---

## Quick start

```bash
git clone <this repo>
cd vaadin-opencore-counter

# build and run the OSS edition
./mvnw -pl counter-community -am install        # one-time
cd counter-community && ./mvnw exec:java        # http://localhost:8080
# Ctrl-C to stop

# build and run the Enterprise edition
cd ../counter-enterprise && ./mvnw install
./mvnw exec:java                                # http://localhost:8080
```

---

## Running OSS mode

The OSS edition has its own `main()` and is a complete Vaadin app on
its own:

```bash
cd counter-community
./mvnw clean install              # builds the production frontend bundle
./mvnw exec:java                  # starts embedded Jetty on :8080
```

Console output:

```
OpenCore Counter started.
Mode is determined by classpath.
Open http://localhost:8080
Loaded features:
- community.core
```

In the browser at <http://localhost:8080> you get:

- the navbar with the drawer toggle and the title **OpenCore Counter**,
- a drawer with **Counter** and **About**,
- the counter view with `+1`, `-1`, `Reset` buttons.

That is everything OSS users get. No Enterprise badge, no History,
no Audit Log, no Export.

---

## Running Enterprise mode

The Enterprise edition reuses the launcher class from the community
module. The only difference is that the Enterprise jar is on the
classpath:

```bash
# 1) Build & install community to the local repository (one time per checkout)
cd counter-community
./mvnw clean install

# 2) Switch to the Enterprise module and build it
cd ../counter-enterprise
./mvnw clean install              # rebuilds the production bundle so
                                  # Grid + TextArea + the navbar badge
                                  # are included

# 3) Start the same launcher class from inside the Enterprise module,
#    with both jars on the classpath
./mvnw exec:java
```

Console output:

```
OpenCore Counter started.
Mode is determined by classpath.
Open http://localhost:8080
Loaded features:
- community.core
- enterprise.counter
```

In the browser:

- the navbar gets a blue pill that reads **ENTERPRISE EDITION** right
  of the title,
- the drawer now also has **History**, **Audit Log** and **Export**,
- every click on `+1` / `-1` / `Reset` is persisted to the in-memory
  `HistoryStore` and `AuditLogStore`,
- `History` shows the rows, `Audit Log` shows human-readable
  messages, `Export` returns a JSON snapshot.

There is no flag, no system property, no config switch. The mode is
**determined entirely by the classpath**.

---

## Tests

Both modules use JUnit Jupiter 6 and the free
`com.vaadin:browserless-test-junit6` library for in-process Vaadin
tests. No browser is launched; the Vaadin internal state tree is
exercised directly.

```bash
# all community tests (domain + service + registry + boundary + UI)
cd counter-community
./mvnw test

# all enterprise tests
cd ../counter-enterprise
./mvnw test
```

Current counts:

| Module             | Test classes | Tests | Notes                                        |
| ------------------ | ------------ | ----- | -------------------------------------------- |
| counter-community  | 7            | 36    | domain · service · registry · boundary · UI  |
| counter-enterprise | 4            | 20    | feature SPI · listeners · export · UI        |
| **Total**          | **11**       | **56**|                                              |

The four test "shapes" used in the repo:

1. **Pure unit tests** — `CounterStateTest`, `CounterServiceTest`,
   `ExportJsonBuilderTest`. No Vaadin, no I/O.
2. **SPI tests** — `FeatureRegistryTest`,
   `EnterpriseFeatureContributionTest`. They construct the registry
   directly and assert about routes, menus, listeners and navbar
   contributions.
3. **Browserless UI tests** — `CounterViewBrowserlessTest`,
   `MainLayoutBrowserlessTest`,
   `EnterpriseMainLayoutBrowserlessTest`. They extend
   `BrowserlessTest`, navigate to a view, locate components via
   `$view(...)` and assert on observable state.
4. **Boundary test** — `CommunityDoesNotReferenceEnterpriseTest`.
   Walks `counter-community/src/main/java` and fails the build if any
   file mentions Enterprise package names.

---

## Mutation testing

The PIT mutation profile is wired in both modules:

```bash
cd counter-community
./mvnw -Pmutation org.pitest:pitest-maven:mutationCoverage

cd ../counter-enterprise
./mvnw -Pmutation org.pitest:pitest-maven:mutationCoverage
```

HTML reports land in `<module>/target/pit-reports/index.html`. The
Vaadin view classes are **excluded** from mutation targets — the
mutants in UI assembly code are noisy and low-value. The mutation
targets are the parts the article actually wants to talk about:
the domain, the extension API, the registry, the listeners and the
export builder.

Why bother? Line coverage tells you whether the line *ran*; mutation
coverage tells you whether the test would *notice* if the line
behaved differently. The numbers under `-Pmutation`:

| Module             | Mutation coverage | Test strength |
| ------------------ | ----------------- | ------------- |
| counter-community  | 100 %             | 100 %         |
| counter-enterprise | 100 %             | 100 %         |

---

## How the extension mechanism actually works

There are four moving parts. Each is small enough to read in a few
minutes; the interesting part is how they fit together.

### 1. `FeatureContribution` (the SPI)

```java
public interface FeatureContribution {
  String id();
  List<RouteContribution> routes();
  List<MenuContribution> menuItems();
  default List<NavbarContribution> navbarItems() { return List.of(); }
  default int order() { return 1000; }
}
```

`RouteContribution(path, viewClass)` is a record. `MenuContribution`
adds a label, a path (which must match an existing route), an order
and an icon name. `NavbarContribution` is the hook that the
Enterprise edition uses to drop the "Enterprise Edition" pill into
the navbar.

A feature can additionally implement `CounterEventFeature`, which
extends `FeatureContribution` with a `counterEventListeners()`
method.

### 2. `META-INF/services` files (the discovery)

Each module ships one line in
`src/main/resources/META-INF/services/com.svenruppert.opencore.counter.extension.FeatureContribution`:

- community: `com.svenruppert.opencore.counter.ui.core.CoreFeatureContribution`
- enterprise: `com.svenruppert.opencore.counter.enterprise.EnterpriseFeatureContribution`

The `FeatureRegistry` constructor does the standard
`ServiceLoader.load(FeatureContribution.class)` call, walks the
result, sorts by `order()`, collects routes, menus, navbar items and
event listeners, and rejects duplicate route paths with a meaningful
error message.

### 3. `OpenCoreRouteInitializer` (dynamic routes)

```java
public class OpenCoreRouteInitializer implements VaadinServiceInitListener {
  @Override
  public void serviceInit(ServiceInitEvent event) {
    RouteConfiguration cfg = RouteConfiguration.forApplicationScope();
    cfg.getHandledRegistry().update(() -> {
      for (RouteContribution route : Application.context().featureRegistry().routes()) {
        if (!cfg.isPathAvailable(route.path())) {
          cfg.setRoute(route.path(), route.viewClass(), MainLayout.class);
        }
      }
    });
  }
}
```

It is registered in
`src/main/resources/META-INF/services/com.vaadin.flow.server.VaadinServiceInitListener`,
so Vaadin picks it up via its own SPI. None of the views in this repo
carry `@Route` annotations — the registration is fully dynamic.

`MainLayout` carries `@Layout("/")` so the production-mode frontend
scanner sees it as a router target, brings its Vaadin component
imports into the bundle, and applies it as the default layout for all
dynamically registered routes.

### 4. `MainLayout` (UI integration point)

```java
@Layout("/")
public class MainLayout extends AppLayout {
  public MainLayout() {
    addToNavbar(createHeader());
    addToDrawer(createDrawer());
  }

  private HorizontalLayout createHeader() {
    H1 title = new H1("OpenCore Counter");
    HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
    var navbarItems = Application.context().featureRegistry().navbarItems();
    if (!navbarItems.isEmpty()) {
      HorizontalLayout extras = new HorizontalLayout();
      extras.getStyle().set("margin-left", "auto");
      for (NavbarContribution c : navbarItems) {
        extras.add(c.componentFactory().get());
      }
      header.add(extras);
      header.setFlexGrow(1, extras);
    }
    return header;
  }
  // ... drawer built from registry.menuItems() the same way
}
```

That is the entire UI integration: the drawer is built from
`MenuContribution`s, the navbar from `NavbarContribution`s. The
layout has zero compile-time references to anything Enterprise.

---

## Extending the demo with your own feature

### As an OSS feature (inside `counter-community`)

1. Write a view, say `SettingsView extends VerticalLayout`.
2. Add a route + menu entry to `CoreFeatureContribution.routes()` and
   `.menuItems()`.
3. Restart. The drawer gets a new "Settings" entry.

No `@Route`, no Vaadin annotation. You are wiring through the SPI.

### As a separate Enterprise / partner module

1. Create a new Maven module that depends on `counter-community`.
2. Implement `FeatureContribution`, optionally `CounterEventFeature`.
3. Drop the FQN into
   `src/main/resources/META-INF/services/com.svenruppert.opencore.counter.extension.FeatureContribution`.
4. Build the module's production frontend bundle with
   `<optimizeBundle>false</optimizeBundle>` (see the Lessons Learned
   section — the byte-code scanner cannot see dynamically wired
   views, so the bundle would otherwise miss any non-trivial
   Vaadin components your views use).
5. Run from your module with `./mvnw exec:java`.

The community module never has to change. You can host your module
in a separate Git repo, ship it as a paid extension, or both.

---

## Lessons learned — the traps we hit on the way

These are the gotchas you only find out about after building this
exact kind of app. They are worth knowing in advance.

### 1. Vaadin's production bundle scanner is static-analysis only

`vaadin-maven-plugin:build-frontend` walks `@Route`/`@Layout`
annotated classes and follows their references to figure out which
Vaadin components to bundle. **Dynamically registered routes are
invisible** to this scanner — and any Vaadin component used only by
such a view (e.g., `Grid` in `HistoryView`, `TextArea` in
`ExportView`) is dropped from the bundle. The view then renders
**blank** in the browser. No error, no console log.

Fix: in the module that contributes dynamic-route views, set

```xml
<configuration>
  <forceProductionBuild>true</forceProductionBuild>
  <optimizeBundle>false</optimizeBundle>
</configuration>
```

on the `vaadin-maven-plugin`. The Enterprise POM in this repo does
exactly that.

### 2. `flow-build-info.json` is written into the JAR but not into `target/classes`

`vaadin-maven-plugin:build-frontend` adds the file to the produced
JAR at packaging time but never writes it back to `target/classes`.
`mvn exec:java` uses `target/classes`, so Vaadin's
`DefaultDeploymentConfiguration.getMode()` finds no token file and
falls back to dev mode — which requires a dev bundle, which is also
not there. Result: 500 Internal Server Error on every page.

Fix: the launcher writes a minimal production token file into
`target/classes/META-INF/VAADIN/config/flow-build-info.json` at
startup if the classloader does not see one. When you `java -jar`
the produced JAR, the file is already inside and this code path is
a no-op.

### 3. `jakarta.servlet-api` ends up `provided` even when you declare it `compile`

Some Vaadin transitive dependency declares `jakarta.servlet-api` as
`provided`, and Maven's mediation lets that win unless you force a
compile-scoped direct declaration. With `exec:java` and no servlet
container that provides it, you get
`NoClassDefFoundError: jakarta/servlet/ServletContext` at launch.

Fix: declare it explicitly with `<scope>compile</scope>` in every
module that runs the launcher.

### 4. Maven 3 (IntelliJ bundled) picks a different Jackson than Maven 4

IntelliJ ships Maven 3 which resolves
`tools.jackson.core:jackson-databind` to `3.0.4` via the transitive
graph. Maven 4 picks `3.1.x`. Vaadin 25.1.1 checks for
`ObjectMapper.treeToValue(JsonNode, Class<?>)` at startup — that
method only exists in 3.1. Result: every Browserless test in
IntelliJ blows up with
`IllegalStateException: The Jackson version on the classpath (3.0.4)
is not compatible`.

Fix: pin Jackson 3.1.2 in `<dependencyManagement>` of both POMs.
Belt-and-braces: configure IntelliJ to use the Maven Wrapper so the
IDE matches the CI build.

### 5. `BrowserlessTest`'s `$view(...)` is scoped to the current view

`$view(SomeType.class).all()` only searches the subtree of the
*innermost* router target (e.g., `CounterView`), not the entire UI.
Parent layouts (`AppLayout`-based `MainLayout`) and anything inside
`addToDrawer(...)` / `addToNavbar(...)` are unreachable that way.

Fix: pull the parent layout out of the active router target chain:

```java
MainLayout layout = UI.getCurrent().getInternals()
    .getActiveRouterTargetsChain().stream()
    .filter(c -> c instanceof MainLayout)
    .map(c -> (MainLayout) c)
    .findFirst()
    .orElseThrow();
```

Then walk `layout.getChildren()` manually.

### 6. Lumo's `theme="badge"` styling is not in the default Vaadin 25 bundle

Setting `theme="badge contrast"` on a `<span>` does not render as a
pill: the CSS for `[theme~='badge']` lives in a separate stylesheet
(`@vaadin/vaadin-lumo-styles/src/global/badge.css`) that Vaadin
loads only when explicitly imported — and even then,
`@CssImport` of an npm-package CSS path goes through Vite as
`?inline`, which makes the bytes available to your app but never
inserts them into a `<style>` tag.

Fix: the `EnterpriseEditionBadge` styles itself with inline styles
on a plain `<span>`. No global CSS, no `@CssImport`, no bundler
risk.

### 7. PIT 1.23 needs the arcmutate `history` plugin if `<withHistory>true</withHistory>`

The default PIT install does not include the history plugin. Either
add it as a `<dependency>` to the plugin, or set
`<withHistory>false</withHistory>` (this repo's choice — mutation
runs are fast enough on this codebase).

---

## IntelliJ IDEA setup

1. Open the **root folder** `vaadin-opencore-counter/`. IntelliJ
   picks up the aggregator POM and imports both modules.
2. Configure IntelliJ to use the Maven Wrapper:
   **Settings → Build, Execution, Deployment → Build Tools → Maven →
   Maven home path: `Use Maven Wrapper`**.
   Without this, IntelliJ runs Maven 3 by default and you get
   subtle dependency-resolution differences (see the Jackson story
   above).
3. The two modules are independent Maven projects: you can build,
   test and release each one without touching the other.

---

## Acceptance summary

The state the build is supposed to be in. If something here is no
longer true, the test suite is supposed to catch it.

- ✅ The community module builds and runs on its own. No
  `counter-enterprise` dependency, transitive or otherwise.
- ✅ The Enterprise module depends on community; the reverse is
  forbidden and verified by
  `CommunityDoesNotReferenceEnterpriseTest`.
- ✅ Routes are registered dynamically by
  `OpenCoreRouteInitializer` from `RouteContribution` records.
  No view in this repo carries `@Route`.
- ✅ Drawer menu items come from
  `FeatureRegistry.menuItems()` — built from `ServiceLoader`-loaded
  `FeatureContribution` implementations.
- ✅ Enterprise views (`History`, `Audit Log`, `Export`) appear in
  the drawer **only** when `counter-enterprise.jar` is on the
  classpath. No flag, no user, no role.
- ✅ Counter changes flow through `CounterEventListener`s
  contributed by Enterprise. `HistoryStore` and `AuditLogStore`
  accumulate them. `ExportView` renders the snapshot as JSON.
- ✅ An "Enterprise Edition" pill appears in the navbar — and
  *only* in the navbar — when the Enterprise module is on the
  classpath.
- ✅ 56 tests across both modules. Both modules report 100 %
  mutation coverage on their targeted packages under `-Pmutation`.
- ✅ The launcher starts without Spring Boot, without Jakarta EE
  platform, without any application server — just `java -cp ...
  CounterApplicationLauncher`.
- ✅ Identical builds in IntelliJ and on the command line, because
  both routes go through the same `./mvnw` (Maven 4.0.0-rc-1).
