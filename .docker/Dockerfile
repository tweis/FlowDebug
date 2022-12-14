FROM php:8.1-apache

ENV PHP_XDEBUG_MODE=off

RUN apt-get update && apt-get install -y \
  git \
  libzip-dev \
  zip \
  && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/local/bin/composer

RUN docker-php-ext-install pdo_mysql \
  && docker-php-ext-install zip

# Install Xdebug
RUN touch /var/log/xdebug.log \
  && chown www-data:www-data /var/log/xdebug.log

RUN pecl install xdebug && docker-php-ext-enable xdebug \
  && echo "xdebug.client_host=host.docker.internal" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.client_port=9003" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.mode=off" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.start_with_request=yes" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.idekey=PHPSTORM" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.log_level=1" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.log=/var/log/xdebug.log" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini \
  && echo "xdebug.max_nesting_level=1000" >> /usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini

# Enable mod_rewrite
RUN a2enmod rewrite

# Set Apache document root
RUN sed -ri -e 's!/var/www/html!/var/www/html/Web!g' /etc/apache2/sites-enabled/000-default.conf

# Raise PHP memory limit (https://flowframework.readthedocs.io/en/stable/TheDefinitiveGuide/PartII/Requirements.html)
RUN echo "memory_limit=256M" >> /usr/local/etc/php/conf.d/php-memory-limit.ini

WORKDIR /var/www/html

EXPOSE 80
