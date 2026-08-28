# XML UI foundation specification

## Navigation

`MainActivity` owns a single `NavHostFragment`. The XML navigation graph starts
at Home and exposes stable destinations for Events and Sales History.

Fragments dispatch navigation actions and render data. They do not calculate
totals, validate purchases, access Room or instantiate concrete dependencies.

## ViewBinding lifecycle

Fragment bindings use `FragmentViewBindingDelegate`.

- Binding is created from the current Fragment view.
- The delegate observes `viewLifecycleOwner`.
- The binding reference is cleared at `onDestroyView`.
- Access without a current Fragment view fails instead of retaining a stale
  hierarchy.

Flow collection from Fragments uses `launchWhenViewStarted`, which delegates to
`repeatOnLifecycle(STARTED)` and stops collection when the view is not visible.

## Reusable states

`StatePanelView` is a passive compound View. It renders:

- loading with a message;
- message states with title, description and icon;
- an optional action callback.

The component contains no feature policy and can be reused by catalog, history,
checkout and receipt surfaces.

## Composition

`CieloTicketsApplication` owns one lazy `AppContainer`. `AppContainerImpl`
creates repository, use-case and Cielo implementations and exposes their
contracts. Features receive dependencies from this composition root instead of
constructing Room or payment adapters.

