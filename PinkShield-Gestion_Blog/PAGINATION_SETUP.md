# Pagination Setup - PinkShield

## What Was Added

Pagination has been added to:
1. **Blog Posts** (index page) - 9 posts per page
2. **Comments** (blog show page) - 10 comments per page

## Package Installed

- `knplabs/knp-paginator-bundle` v6.10.0

## Changes Made

### 1. BlogController.php
- Added `PaginatorInterface` dependency
- Updated `index()` method to paginate blog posts
- Updated `show()` method to paginate comments (only top-level comments, replies are shown under their parent)

### 2. BlogPostRepository.php
- Added `searchAndSortQueryBuilder()` method that returns a QueryBuilder instead of results
- Kept original `searchAndSort()` method for backward compatibility

### 3. Templates

**templates/blog/index.html.twig:**
- Changed `{% for post in posts %}` to `{% for post in pagination %}`
- Added pagination controls: `{{ knp_pagination_render(pagination) }}`
- Added custom CSS for pagination styling

**templates/blog/show.html.twig:**
- Changed `{% for comment in post.comments %}` to `{% for comment in pagination %}`
- Added pagination controls for comments
- Added custom CSS for pagination styling

### 4. Configuration

**config/packages/knp_paginator.yaml:**
- Configured to use Bootstrap 5 pagination template
- Set page range to 5
- Configured query parameter names

## Features

### Blog Posts Pagination
- Shows 9 posts per page in a 3-column grid
- Maintains search and sort functionality
- URL format: `/blog?page=2&q=search&sort=latest`

### Comments Pagination
- Shows 10 top-level comments per page
- Replies are always shown under their parent comment (not paginated separately)
- URL format: `/blog/1?page=2`

### Styling
- Custom PinkShield theme colors (#C41E3A)
- Hover effects and transitions
- Active page highlighting
- Responsive design

## How It Works

1. **Blog Posts:**
   - Controller creates a QueryBuilder from repository
   - Paginator wraps the QueryBuilder
   - Template loops through `pagination` instead of `posts`
   - Pagination controls automatically handle page numbers

2. **Comments:**
   - Controller creates QueryBuilder for top-level comments only
   - Filters by blog post and `parentComment IS NULL`
   - Replies are fetched through the parent comment relationship
   - Each parent comment shows all its replies (not paginated)

## Testing

1. Visit `/blog` - Should see 9 posts per page
2. Add more than 9 blog posts to see pagination
3. Visit a blog post with 10+ comments to see comment pagination
4. Test search and sort with pagination

## Customization

### Change Items Per Page

**Blog Posts:**
```php
$pagination = $paginator->paginate(
    $queryBuilder,
    $request->query->getInt('page', 1),
    9 // Change this number
);
```

**Comments:**
```php
$pagination = $paginator->paginate(
    $queryBuilder,
    $request->query->getInt('page', 1),
    10 // Change this number
);
```

### Change Pagination Style

Edit `config/packages/knp_paginator.yaml`:
```yaml
template:
    pagination: '@KnpPaginator/Pagination/bootstrap_v5_pagination.html.twig'
    # Or use: twitter_bootstrap_v4_pagination.html.twig
    # Or create your own custom template
```

### Change Colors

Edit the CSS in templates:
```css
.pagination .page-link {
    color: #C41E3A; /* Change this */
}

.pagination .page-link:hover {
    background-color: #C41E3A; /* Change this */
}
```

## Benefits

- ✅ Better performance (loads only needed items)
- ✅ Better UX (faster page loads)
- ✅ SEO friendly (proper page URLs)
- ✅ Mobile friendly (less scrolling)
- ✅ Maintains all existing functionality (search, sort, filters)
